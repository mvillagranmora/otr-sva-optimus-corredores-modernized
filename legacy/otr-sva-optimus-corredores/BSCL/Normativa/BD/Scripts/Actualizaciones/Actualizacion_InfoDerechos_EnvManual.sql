SET SCHEMA "BSCL";


---Se quita de la pauta :(
UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso" SET  "IdEstado"='INA' WHERE "CodigoProceso" = 'GENERARINFODERECHOS_PAUTA';
UPDATE BSCL."BSCL.Plataforma.BD::PAU.Proceso" SET  "IdEstado"='INA' WHERE "CodigoProceso" = 'GENERAR1064PAUTA_R';


 INSERT INTO BSCL."BSCL.Normativa.BD::NOR.TipoParametros"
("TipoParametro", "Entidad", "Nombre", "EsMasivo", "EsInterno")
SELECT 'ENVIO_TANDEM', 'SOLICITUD', 'Envio a Tandem', 'N', 'N'
FROM dummy WHERE 
 NOT EXISTS (SELECT 1 FROM BSCL."BSCL.Normativa.BD::NOR.TipoParametros" N WHERE N."TipoParametro"='ENVIO_TANDEM');
 
 
  
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_ENVIO_BURSATIL',
'CL',
'Envio Bursatil',
'ENVBUR'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_ENVIO_BURSATIL');
 