# Compilacion PLUGIN_OPTIMUS

Proceso de compilacion par 

Existen dos posibles compilaciones, la primera es para subir a los servidores:
`gradlew -b PLUGIN_OPTIMUS.gradle clean jar`
se creara un jar en la carpeta build/libs/PLUGIN_OPTIMUS.jar

La segunda es para construir un java standalone quer permita ejecutar las clases java con metodo main:
`gradlew -b PLUGIN_OPTIMUS.gradle clean shadowJar`

se creara un jar en la carpeta build/libs/PLUGIN_OPTIMUS-all.jar, para ejecutar una clase:
`java -cp build/libs/PLUGIN_OPTIMUS-all.jar cl.bcs.normativa.cartola.utils.CargaCartolasSebra bestado C:\BCS\CAR_HIS\BESTADO`

## troubleshooting

Configurar correctamente el ambiente como esta descrito en [WIKI OPTIMUS](http://161.238.0.134:8080/bin/view/Main/Instalacion%20de%20Componentes%20Optimus%20%2B%20SAP%20B1/Instalaci%C3%B3n%20Optimus%20Corredores/)