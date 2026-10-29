@echo on
move /Y "%JAVA_HOME%\jre\lib\security\cacerts" "%JAVA_HOME%\jre\lib\security\cacerts_org"
copy certs\cacerts "%JAVA_HOME%\jre\lib\security\"