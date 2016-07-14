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
package iot.agile.http;

import iot.agile.Device;
import iot.agile.DeviceManager;
import iot.agile.Protocol;
import iot.agile.ProtocolManager;
import java.util.HashMap;
import java.util.Map;
import org.freedesktop.dbus.DBusInterface;

/**
 *
 * @author Luca Capra <lcapra@create-net.org>
 */
public class PathPattern {
  
  static public Map<String, Class<? extends DBusInterface>> map = new HashMap();
  
  static public Class get(String path) {
    return get(path, "");
  }
  
  static public Class get(String path, String prefix) {
    
    if(map.isEmpty()) {
      
      map.put("devices", DeviceManager.class);
      map.put("device", Device.class);
      
      map.put("protocol", Protocol.class);
      map.put("protocols", ProtocolManager.class);

    }
    
    String[] parts = path.replace(prefix, "").split("/");
    return map.getOrDefault(parts[0], null);
  }
  
}
