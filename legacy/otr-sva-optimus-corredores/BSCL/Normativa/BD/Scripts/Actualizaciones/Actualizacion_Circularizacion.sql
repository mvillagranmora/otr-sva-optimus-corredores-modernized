SET SCHEMA "BSCL";

/*SCRIPT CIRCULARIZACION*/
INSERT INTO BSCL."BSCL.Normativa.BD::NOR.EstadoNormativa"
("IdEstadoNormativa", "IdEstado", "NombreEntidad")
SELECT 17, 'EPR', 'SOLICITUD-CIRCULAR' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Normativa.BD::NOR.EstadoNormativa" WHERE  "IdEstado"='EPR');

INSERT INTO BSCL."BSCL.Normativa.BD::NOR.EstadoNormativa"
("IdEstadoNormativa", "IdEstado", "NombreEntidad")
SELECT 18, 'PRO', 'SOLICITUD-CIRCULAR' FROM DUMMY
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Normativa.BD::NOR.EstadoNormativa" WHERE  "IdEstado"='PRO');

--cl.bcs.normativa.jobs.BcsJobGrabarCertificadoCliente
--cl.bcs.normativa.jobs.BcsJobCircularizacionGrabarCertificado
--SELECT *FROM   "BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='GRABARCARTACIRCULARIZACIONMASIVO';    

UPDATE  "BSCL.Plataforma.BD::BAT.TipoPrograma" SET "NombreClasePrograma"='cl.bcs.normativa.jobs.BcsJobCircularizacionGrabarCertificado'
WHERE "IdTipoPrograma"='GRABARCARTACIRCULARIZACIONMASIVO';


INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Diccionario" ( "CodDiccionario","CodLenguaje","Descripcion","Abreviado" )
SELECT  
'UI_PDF_MASIVO',
'CL',
'PDF Masivo',
'PDFMAS'
FROM DUMMY 
WHERE NOT EXISTS (SELECT 1 FROM  BSCL."BSCL.Plataforma.BD::PLA.Diccionario" WHERE  "CodDiccionario"='UI_PDF_MASIVO');




	