/**
*  Copyright 2023-2024 Bloodtick, kkossev
*
*  Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License. You may obtain a copy of the License at:
*
*      http://www.apache.org/licenses/LICENSE-2.0
*
*  Unless required by applicable law or agreed to in writing, software distributed under the License is distributed
*  on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License
*  for the specific language governing permissions and limitations under the License.
*
* 
*  ver. 1.0.0 2022-04-22 kkossev - inital version - removed Battery; 
* 
*                        TODO: add IlluminanceMeasurement
*                        TODO: remove TemperatureMeasurement, remove Battery
*/
@SuppressWarnings('unused')
public static String version()   {return "1.0.0"}
public static String timeStamp() {return "10/12/2024 11:55 PM"}

metadata 
{
    definition(name: "Replica Aqara FP2", namespace: "replica", author: "kkossev", importUrl:"https://raw.githubusercontent.com/bloodtick/Hubitat/main/hubiThingsReplica/devices/replicaMotionSensor.groovy")
    {
        capability "Actuator"
        capability "Configuration"
        capability "MotionSensor"                // ST: "movementSensor"
        // TODO                                  // ST: "multipleZonePresence"
        // TODO                                  // ST: "presenceSensor"
        capability "Refresh"                     // ST: "refresh"
        //capability "TemperatureMeasurement"
        capability "IlluminanceMeasurement"     // ST: "illuminanceMeasurement"

        attribute "healthStatus", "enum", ["offline", "online"]

        command "inactive"
        command "active"
        //command "setTemperature", [[name: "temperature*", type: "NUMBER", description: "Set Temperature in local scale °C or °F"]]
        command "setIlluminance", [[name: "illuminance*", type: "NUMBER", description: "Set Illuminance in lux"]]
    }
    preferences {
        input(name:"deviceInfoDisable", type: "bool", title: "Disable Info logging:", defaultValue: false)
    }
}

def installed() {
	initialize()
}

def updated() {
	initialize()    
}

def initialize() {
    updateDataValue("triggers", groovy.json.JsonOutput.toJson(getReplicaTriggers()))
    updateDataValue("commands", groovy.json.JsonOutput.toJson(getReplicaCommands()))
    refresh()
}

def configure() {
    logInfo "${device.displayName} configured default rules"
    initialize()
    updateDataValue("rules", getReplicaRules())
    sendCommand("configure")
}

// Methods documented here will show up in the Replica Command Configuration. These should be mostly setter in nature. 
Map getReplicaCommands() {
    return ([ "setMotionValue":[[name:"motion*",type:"ENUM"]], "setMotionActive":[], "setMotionInactive":[],             
              "setIlluminanceValue":[[name:"illuminance*",type:"NUMBER"]], 
              "setHealthStatusValue":[[name:"healthStatus*",type:"ENUM"]]
            ])
}

def setIlluminanceValue(value) {
    String descriptionText = "${device.displayName} illuminance is $value lux"
    sendEvent(name: "illuminance", value: value, unit: "lx", descriptionText: descriptionText)
    logInfo descriptionText
}

def setMotionValue(value) {
    String descriptionText = "${device.displayName} motion is $value"
    sendEvent(name: "motion", value: value, descriptionText: descriptionText)
    logInfo descriptionText
}

def setMotionActive() {
    setMotionValue("active")
}

def setMotionInactive() {
    setMotionValue("inactive")    
}

def setHealthStatusValue(value) {    
    sendEvent(name: "healthStatus", value: value, descriptionText: "${device.displayName} healthStatus set to $value")
}

// Methods documented here will show up in the Replica Trigger Configuration. These should be all of the native capability commands
Map getReplicaTriggers() {
    return ([ "inactive":[] , "active":[], 
        "setIlluminance":[[name:"illuminance*",type:"NUMBER"]], 
        "refresh":[]])
}

private def sendCommand(String name, def value=null, String unit=null, data=[:]) {
    data.version=version()
    parent?.deviceTriggerHandler(device, [name:name, value:value, unit:unit, data:data, now:now()])
}

def inactive() {
    sendCommand("inactive")    
}

def active() {
    sendCommand("active")    
}
             
def setIlluminance(lux) {
    sendCommand("setIlluminance", lux, "lx")    
}

void refresh() {
    sendCommand("refresh")
}

String getReplicaRules() {
    return """{"components":[{"command":{"label":"command: setHealthStatusValue(healthStatus*)","name":"setHealthStatusValue","parameters":[{"name":"healthStatus*","type":"ENUM"}],"type":"command"},"mute":true,"trigger":{"additionalProperties":false,"attribute":"healthStatus","capability":"healthCheck","label":"attribute: healthStatus.*","properties":{"value":{"title":"HealthState","type":"string"}},"required":["value"],"type":"attribute"},"type":"smartTrigger"},{"command":{"capability":"refresh","label":"command: refresh()","name":"refresh","type":"command"},"trigger":{"label":"command: refresh()","name":"refresh","type":"command"},"type":"hubitatTrigger"},{"command":{"label":"command: setIlluminanceValue(illuminance*)","name":"setIlluminanceValue","parameters":[{"name":"illuminance*","type":"NUMBER"}],"type":"command"},"trigger":{"additionalProperties":false,"attribute":"illuminance","capability":"illuminanceMeasurement","label":"attribute: illuminance.*","properties":{"unit":{"default":"lux","enum":["lux"],"type":"string"},"value":{"maximum":100000,"minimum":0,"type":"number"}},"required":["value"],"type":"attribute"},"type":"smartTrigger"},{"command":{"label":"command: setMotionActive()","name":"setMotionActive","type":"command"},"trigger":{"additionalProperties":false,"attribute":"presence","capability":"presenceSensor","dataType":"ENUM","label":"attribute: presence.present","properties":{"value":{"title":"PresenceState","type":"string"}},"required":["value"],"type":"attribute","value":"present"},"type":"smartTrigger"},{"command":{"label":"command: setMotionInactive()","name":"setMotionInactive","type":"command"},"trigger":{"additionalProperties":false,"attribute":"presence","capability":"presenceSensor","dataType":"ENUM","label":"attribute: presence.not present","properties":{"value":{"title":"PresenceState","type":"string"}},"required":["value"],"type":"attribute","value":"not present"},"type":"smartTrigger"}],"version":1}
}"""
}

private logInfo(msg)  { if(settings?.deviceInfoDisable != true) { log.info  "${msg}" } }
private logDebug(msg) { if(settings?.deviceDebugEnable == true) { log.debug "${msg}" } }
private logTrace(msg) { if(settings?.deviceTraceEnable == true) { log.trace "${msg}" } }
private logWarn(msg)  { log.warn   "${msg}" }
private logError(msg) { log.error  "${msg}" }
