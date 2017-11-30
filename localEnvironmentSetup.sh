# Used for backing services like the PostgreSQL database
export VCAP_APPLICATION={}
export VCAP_SERVICES='{"mongodb": [{"name": "test","credentials": {"hostname": "127.0.0.1","port": "27017","username": "testuser","password": "test123!","dbname": "test","uri": "mongodb://testuser:test123!@localhost:27017/test"},"syslog_drain_url": null,"volume_mounts": [],"label": "mongodb","provider": null,"tags": ["mongodb","document"],"plan": "v3.0-dev"}]}'

# Used for dependent service call
#export USER_ROUTE=https://bulletinboard-users-course.cfapps.sap.hana.ondemand.com

# Overwrite logging library defaults
export APPENDER=STDOUT
export LOG_APP_LEVEL=ALL

