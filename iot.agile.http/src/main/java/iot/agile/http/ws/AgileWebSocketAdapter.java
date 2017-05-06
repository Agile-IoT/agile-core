/*******************************************************************************
 * Copyright (C) 2017 Create-Net / FBK.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Create-Net / FBK - initial API and implementation
 ******************************************************************************/
/*
 * Copyright 2016 CREATE-NET
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package iot.agile.http.ws;

import java.io.IOException;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.eclipse.jetty.websocket.api.Session;

import org.freedesktop.dbus.DBusConnection;
import org.freedesktop.dbus.DBusSigHandler;
import org.freedesktop.dbus.exceptions.DBusException;

import iot.agile.Device;
import iot.agile.Device.NewSubscribeValueSignal;
import iot.agile.object.StatusType;
import iot.agile.object.DeviceStatusType;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Csaba Kiraly <kiraly@fbk.eu>
 */
@WebSocket
public class AgileWebSocketAdapter extends WebSocketAdapter {

  private Session session;
  private String deviceID = null;
  private String sensorName = null;
  private DBusSigHandler sigHandler;
  private boolean subscribed = false;

  private ObjectMapper mapper = new ObjectMapper();

  protected Logger logger = LoggerFactory.getLogger(AgileWebSocketAdapter.class);

  @Override
  public void onWebSocketConnect(Session sess) {
    session = sess;
    String uripath = sess.getUpgradeRequest().getRequestURI().getPath();
    logger.info("New websocket connection from {} for {}", sess.getRemoteAddress(), uripath);

    Pattern p = Pattern.compile("^/ws/device/([^/]+)/([^/]+)/subscribe");
    Matcher m = p.matcher(uripath);

    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);

      if (m.matches()) {
        deviceID = m.group(1);
        sensorName = m.group(2);

        String busname = Device.AGILE_INTERFACE;
        String path = "/" + Device.AGILE_INTERFACE.replace(".", "/")  + "/" + deviceID;
        Device device = connection.getRemoteObject(busname, path, Device.class);

        if (device.Status().getStatus().equals(DeviceStatusType.CONNECTED.toString())) {
          device.Subscribe(sensorName);
          subscribed = true;

          sigHandler = new DBusSigHandler<Device.NewSubscribeValueSignal>() {
            @Override
            public void handle(NewSubscribeValueSignal sig) {
              if (sig.record.getDeviceID().equals(deviceID) && sig.record.getComponentID().equals(sensorName)) {
                logger.debug("http: New value {}%n", sig.record);
                try {
                  session.getRemote().sendString(mapper.writeValueAsString(sig.record));
                } catch (IOException e) {
                  e.printStackTrace();
                }
              }
            }
          };
          connection.addSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
        } else {
          logger.info("Device {} not connected", deviceID);
          sess.close(404, "Device not connected");
        }

      } else {

        sigHandler = new DBusSigHandler<Device.NewSubscribeValueSignal>() {
          @Override
          public void handle(NewSubscribeValueSignal sig) {
            try {
              session.getRemote().sendString(mapper.writeValueAsString(sig.record));
            } catch (IOException e) {
              e.printStackTrace();
            }
          }
        };
        connection.addSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
      }
    } catch (org.freedesktop.DBus.Error.ServiceUnknown e) {
      sess.close(404, "Device not found");
    } catch (org.freedesktop.DBus.Error.UnknownObject e) {
      sess.close(404, "Device not found");
    } catch (DBusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    try {
      DBusConnection connection = DBusConnection.getConnection(DBusConnection.SESSION);

      if (sigHandler != null) {
        connection.removeSigHandler(Device.NewSubscribeValueSignal.class, sigHandler);
        sigHandler = null;
      }

      if (deviceID != null) {
        logger.info("closing {}/{} reason:{}/{}", deviceID, sensorName, statusCode, reason);

        if (subscribed) {
          String busname = Device.AGILE_INTERFACE;
          String path = "/" + Device.AGILE_INTERFACE.replace(".", "/")  + "/" + deviceID;
          Device device = connection.getRemoteObject(busname, path, Device.class);
          device.Unsubscribe(sensorName);
          subscribed = false;
        }
      } else {
        logger.info("closing reason:{}/{}", deviceID, sensorName, statusCode, reason);
      }
    } catch (DBusException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }
}
