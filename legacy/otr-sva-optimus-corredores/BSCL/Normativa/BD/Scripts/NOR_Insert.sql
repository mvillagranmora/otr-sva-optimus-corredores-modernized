SET SCHEMA "BSCL";

--SELECT * FROM "BSCL.Normativa.BD::NOR.Normativa";

--SELECT * FROM "BSCL.Normativa.BD::NOR.TipoParametros"

--SELECT * FROM "BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"='SEIL';

--DELETE FROM "BSCL.Normativa.BD::NOR.Normativa" WHERE "IdNormativa"='SEIL';



/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::NOR.EstadoNormativa";
DELETE FROM "BSCL.Normativa.BD::NOR.Normativa";
DELETE FROM "BSCL.Normativa.BD::NOR.Configuracion";
DELETE FROM "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion";
DELETE FROM "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion";
DELETE FROM "BSCL.Normativa.BD::NOR.TipoParametros";


INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (1,'APD','CIRCULARIZACION-RESPUESTA');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (2,'RZD','CIRCULARIZACION-RESPUESTA');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (3,'PEN','CIRCULARIZACION-RESPUESTA');

INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (4,'ENV','CIRCULARIZACION-NOTIFICACION');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (5,'PEN','CIRCULARIZACION-NOTIFICACION');


INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (6,'PEN','SOLICITUD-CIRCULAR');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (7,'TER','SOLICITUD-CIRCULAR');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (8,'ERR','SOLICITUD-CIRCULAR');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (13,'INA','SOLICITUD-CIRCULAR');

INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (9,'ENV','EJECUCIONORDENES-NOTIFICACION');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (10,'PEN','EJECUCIONORDENES-NOTIFICACION');

INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (11,'GEN','EJECUCIONORDENES-CARTA');
INSERT INTO "BSCL.Normativa.BD::NOR.EstadoNormativa" ("IdEstadoNormativa", "IdEstado", "NombreEntidad")
VALUES (12,'PEN','EJECUCIONORDENES-CARTA');

/*==============================================================*/
/* Insert Table: Normativa                                      */
/*==============================================================*/
--INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
--VALUES('CIR1237','Circular 1237','ACT','DIRECTA','SVS');
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('CIR1064','Circular 1064','ACT','CLASIFICACION','SVS');

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('NCG69','Norma de Carácter General 69','ACT','DIRECTA','SVS');
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('ART63','Articulo 63','ACT','DIRECTA','SVS');
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('NCG380','Norma de Carácter General 380','ACT','CLASIFICACION','SVS');
--INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
--VALUES('DJ1820','Declaración Jurada 1820','ACT','DIRECTA','SII');
--INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
--VALUES('DJ1829','Declaración Jurada 1829','ACT','DIRECTA','SII');
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")  
VALUES('ART171CartaDos','CARTA 2 ARTICULO 171','ACT','CLASIFICACION','SVS','CARLOS PAVEZ TOLOSA','SUPERINTENDENTE VALORES Y SEGUROS ');
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('ART171CartaUno','CARTA 1 ARTICULO 171','ACT','CLASIFICACION','BCS','MARCELO ROJAS SOTO','Gerente General'); 
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('CIR63','CIRCULAR 63','ACT','CLASIFICACION','BCS','MARCELO ROJAS SOTO','Gerente General');   
INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('UAF','Unidad de Análisis Financiero (UAF)','ACT','CLASIFICACION','BCS','',''); 

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('CIRCULARIZACION','CIRCULARIZACION','ACT','','BCS','',''); 

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('INFOTRANOFIC','Informe de Transacciones por Oficio','ACT','','BCS','',''); 

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('CERTCLIENTE','Certificados Clientes','ACT','','BCS');

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('INFDERECH','Informe Derechos','ACT','','BCS');

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino")
VALUES('ANEXO','Anexos','ACT','','SVS');

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('EJECUCIONORDEN','Ejecución de Ordenes','ACT','','SVS','',''); 

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('CIR1518','Circular 1518 - BCS','ACT','','BCS','',''); 

INSERT INTO "BSCL.Normativa.BD::NOR.Normativa"( "IdNormativa","NombreNormativa","IdEstado","MetodoRelacionado","EntidadDestino","NombreDestinatario", "CargoDestinatario")
VALUES('CIR1720','Circular 1720 - SVS','ACT','','SVS','',''); 

SELECT * FROM  "BSCL.Normativa.BD::NOR.Normativa"
/*==============================================================*/
/* Insert Table: Configuracion                                  */
/*==============================================================*/

-- Almacenamiento de Archivos
--INSERT INTO bscl."BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
--VALUES(1, 'RutaArchivosNCG380', 'Ruta utilizada para almacenar archivos de la NCG380', '/mnt/optimus/des/empresa1/nor');

INSERT INTO bscl."BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'RaizArchivos', 'Ruta raiz de los archivos', '/mnt/optimus/des/empresa1/nor'); 

INSERT INTO bscl."BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'SubRutaArchivosModulo', 'Sub ruta de los archivos para el modulo', 'nor/'); 

-- Configuracion Declaraciones
INSERT INTO bscl."BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'CodigoEmpresaDJInternet', 'Codigo Empresa para DJ por Internet', '91'); 

INSERT INTO bscl."BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor")
VALUES(1, 'CodigoEmpresaDJMagnetico', 'Codigo Empresa para DJ por medio Mágnetico', '80'); 

---ART171_C2
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'VAL_MONTO_ART171', 'Monto que debe alcanzar la transaccion para ser informada en la Carta 2 Art.171', '500','ART171CartaDos'); 

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'MON_MONTO_ART171', 'Moneda monto que debe alcanzar la transaccion para ser informada en la Carta 2 Art.171', 'UF','ART171CartaDos');

--Cirularizacion

--INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
--VALUES(1, 'NOTIFiCA_NOMINA_CIRCULARIZA', 'Genera notificacion al procesar la nomina de circularización, Valor[S/N]', 'N','CIRCULARIZACION');


---UAFROE
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'VAL_MONTO_UAFROE', 'Monto que debe alcanzar la operacion para ser informado en el ROE', '450','UAF'); 

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'MON_MONTO_UAFROE', 'Moneda monto que debe alcanzar la operacion para ser informado en el ROE', 'UF','UAF');

------UAFDOF

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'VAL_MONTO_UF_UAFDOF', 'Monto en UF que debe alcanzar la operacion para ser informado en el DOF', '450','UAF');

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'VAL_MONTO_DOLAR_UAFDOF', 'Monto en DOLAR que debe alcanzar la operacion para ser informado en el DOF', '15000','UAF');  


INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'EVAL_DOF_UAFDOF', 'Determina si se realiza la evaluacion DOF [S/N] (defecto N)', 'S','UAF');

------UAF SOI

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'EVAL_PLAPRO_UAFSOI', 'Determina si se realiza la evaluacion de SOI, a la operación segun plantilla producto base del portafolio [S/N] (defecto N)', 'S','UAF');

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'NOTIF_PLAPRO_UAFSOI', 'Determina si se envía notificacion a Cumplimiento cuando la operacion no corresponde a la plantilla producto base (inicial) del portafolio [S/N] (defecto N)', 'S','UAF');  

---GENERALES
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'GEN_IDPERSONA_CUMPLIMIENTO1', 'Identifica a la Persona como Oficial de cumplimiento, si es cero no se Notifica', '621875','');  

---PROCESO CALCULO SOI (ESTADISTICA CLIENTE)
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_HAB_ACTMONTOPROM_CONPROM', 'Determina si se realiza o no el recalculo del promedio una vez que ha transcurrido la cantidad de dias configurados [S/N] (defecto N)​', 'S','UAF'); 

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_HAB_BUSCLI_SINPROM', '​Determina si se realiza o no la busqueda diaria de operaciones para Clientes que no tienen Promedio Calculado  [S/N] (defecto N)', 'S','UAF'); 

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_CAL_ACTMONTOPROM_CANTDIAS', 'Identifica la cantidad de dias habiles que deben pasar para que el proceso considere recalcular el Monto promedio diario de los Clientes', '5','UAF'); 

INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_CAL_PERMONTO_CANTDIAS', 'Identifica la cantidad de dias a considerar en el calculo del promedio de montos operados de cada cliente', '365','UAF'); 
--PROCESOEVALUACION SOI
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_HAB_EVALUACION_MONTOPROM', 'Determina si se realiza o no el proceso de evaluacion para operaciones Inusuales [S/N] (defecto N)', 'S','UAF'); 
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_EVA_MONTOPROMEDIO_FACTOR', 'Determina el Factor de Tolerancia en que el Monto de la operacion podria estar por sobre el Monto Promedio diario del cliente', '0.01','UAF'); 
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_EVA_MONTOMAXIMO_FACTOR', 'Determina el Factor de Tolerancia en que el Monto de la operacion podria estar por sobre el Monto maximo realizado anteriormente por el cliente', '0.01','UAF'); 
INSERT INTO "BSCL.Normativa.BD::NOR.Configuracion"("IdEmpresa", "CodigoParametro", "Descripcion", "Valor", "IdNormativa")
VALUES(1, 'PRO_EVA_PATRIMONIO_FACTOR', 'Determina el Factor de Tolerancia en que el Monto de la operacion podria estar por sobre el Patrimonio ingresado en la Ficha', '0.01','UAF'); 




/*==============================================================*/
/* Insert Table: TipoClasificacionRelacion                      */
/*==============================================================*/
/*
---RELACIONADOS 'Carta 2 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C2_1', 'ART171CartaDos', 'GG', 'Gerente general','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C2_2', 'ART171CartaDos', 'GA', 'Gerente de área o subgerente','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C2_3', 'ART171CartaDos', 'JM', 'Jefe de mesa de dinero','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C2_4', 'ART171CartaDos', 'OM', 'Operador de mesa de dinero','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C2_5', 'ART171CartaDos', 'OR', 'Otra relación','ACT'); ---Distinta de las anteriores

---RELACIONADOS 'Carta 1 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C1_1', 'ART171CartaUno', 'EMPREL', 'Empleado Relacionado','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('ART171_C1_2', 'ART171CartaUno', 'PERREL', 'Persona Relacionada','ACT'); ---Distinta de las anteriores


---RELACIONADOS 'Circular 63'
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('CIR63_1', 'CIR63', 'RELLAB', 'Laboral','ACT'); 
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('CIR63_2', 'CIR63', 'PERREL', 'Otras Personas','ACT'); ---Distinta de las anteriores
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('CIR63_3', 'CIR63', 'NOINFO', 'No Informada','ACT'); ---Sin Relacion



---RELACIONADOS 'NCG380'
INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_1', 'NCG380', 'Autorizado', 'Autorizado a dar ordenes','ACT'); 

INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_2', 'NCG380', 'Empleador', 'Empleador','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_3', 'NCG380', 'Apoderado', 'Apoderado','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_4', 'NCG380', 'Admin', 'Administrador','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_5', 'NCG380', 'Benefi', 'Beneficiario','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion"
("IdClasificacionRelacion", "IdNormativa", "CodigoRelacionNorma", "DescripcionRelacionNorma", "IdEstado")
VALUES('NCG380_6', 'NCG380', 'PartiSoc', 'Participacion Sociedades','ACT');

*/



/*==============================================================*/
/* Insert Table: NormativaRelacionClasificacion                 */
/*==============================================================*/
/*
---RELACIONADOS 'Carta 2 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(1, 'ART171_C2_1', 'GERGEN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(2, 'ART171_C2_2', 'GERSUB','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(3, 'ART171_C2_3', 'JEFMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(4, 'ART171_C2_4', 'OPEMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(5, 'ART171_C2_5', 'OTRARE','ACT');
---RELACIONADOS 'Carta 1 Articulo 171'
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(6, 'ART171_C1_2', 'OTRARE','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(7, 'ART171_C1_1', 'JEFCUS','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(8, 'ART171_C1_1', 'PERADM','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(9, 'ART171_C1_1', 'TRA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(10, 'ART171_C1_1', 'TES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(11, 'ART171_C1_1', 'AGE','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(12, 'ART171_C1_1', 'SUP','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(13, 'ART171_C1_1', 'OPEOTC','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(14, 'ART171_C1_1', 'ASEFIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(15, 'ART171_C1_1', 'DIR','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(16, 'ART171_C1_1', 'GERENT','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(17, 'ART171_C1_1', 'GERGEN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(18, 'ART171_C1_1', 'GERSUB','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(19, 'ART171_C1_1', 'ADMIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(20, 'ART171_C1_1', 'EMPLEA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(21, 'ART171_C1_1', 'JEFMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(22, 'ART171_C1_1', 'OPEMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(23, 'ART171_C1_1', 'ADMIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(24, 'ART171_C1_1', 'EMPLEA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(25, 'ART171_C1_1', 'JEFMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(26, 'ART171_C1_1', 'OPEMES','ACT');


---RELACIONADOS 'Circular 63'
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(27, 'CIR63_2', 'OTRARE','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(28, 'CIR63_1', 'JEFCUS','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(29, 'CIR63_1', 'PERADM','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(30, 'CIR63_1', 'TRA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(31, 'CIR63_1', 'TES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(32, 'CIR63_1', 'AGE','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(33, 'CIR63_1', 'SUP','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(34, 'CIR63_1', 'OPEOTC','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(36, 'CIR63_1', 'ASEFIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(37, 'CIR63_1', 'DIR','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(38, 'CIR63_1', 'GERENT','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(39, 'CIR63_1', 'GERGEN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(40, 'CIR63_1', 'GERSUB','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(41, 'CIR63_1', 'ADMIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(42, 'CIR63_1', 'EMPLEA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(43, 'CIR63_1', 'JEFMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(44, 'CIR63_1', 'OPEMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(45, 'CIR63_1', 'ADMIN','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(46, 'CIR63_1', 'EMPLEA','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(47, 'CIR63_1', 'JEFMES','ACT');
INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(48, 'CIR63_1', 'OPEMES','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(49, 'CIR63_3', 'SINREL','ACT');



---RELACIONADOS 'NCG380'

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(50, 'NCG380_1', 'AUTZDO','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(51, 'NCG380_2', 'CEMDOR','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(52, 'NCG380_3', 'APO','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(53, 'NCG380_4', 'ADMIN','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(54, 'NCG380_5', 'BENEMP','ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"("IdNormativaRelacionClasificacion", "IdClasificacionRelacion", "IdTipoRelacion","IdEstado")
VALUES(55, 'NCG380_6', 'PARSOC','ACT');

*/
SET SCHEMA "BSCL";
SELECT * FROM  "BSCL.Normativa.BD::NOR.TipoParametros"; 
/*==============================================================*/
/* Insert Table: TIPO PARAMETROS     					    */
/*==============================================================*/

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('EMP','SOLICITUD','Empresa','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUT','SOLICITUD','RUT','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUTAUT','SOLICITUD','RUT Autori.','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('NEMO','SOLICITUD','Nemo','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUTEMPL','SOLICITUD','RUT Empleado','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IDCLI','SOLICITUD','Id Cliente','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('MONTO','SOLICITUD','Monto','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IDEMPLEADO','SOLICITUD','Id Empleado','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FINI','SOLICITUD','Fecha Inicio','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FTER','SOLICITUD','Fecha Termino','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('OFI','SOLICITUD','Oficio','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('MCDO','SOLICITUD','Mercado','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TIPINF','SOLICITUD','Tipo Información','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('CLAINS','SOLICITUD','Clasif. Instrumento','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IDSUCURSAL','SOLICITUD','Id Sucursal','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IDEMI','SOLICITUD','Id Emisor','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IDINS','SOLICITUD','Id Instrumento','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RELA','SOLICITUD','Relación','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('PERI','SOLICITUD','Periodo','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('USUA','SOLICITUD','Id Usuario','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('EsPauta','SOLICITUD','Solicitud en pauta','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FREA','SOLICITUD','Fecha Realizacion','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FEDES','SOLICITUD','Fecha Desde','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FEHAS','SOLICITUD','Fecha Hasta','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('OBS','SOLICITUD','Observaciones','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('NOMSAL','SOLICITUD','Nombre de Salida','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo","EsInterno")
VALUES('EXTSAL','SOLICITUD','Extención de Salida','N','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo","EsInterno")
VALUES('COMSAL','SOLICITUD','Salida Comprimida','N','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo","EsInterno")
VALUES('NOMCOLSAL','SOLICITUD','Salida con nombres de columnas','N','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo","EsInterno")
VALUES('TIPINS','SOLICITUD','Tipo Instrumento','N','N');


INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FEJEC','SOLICITUD','Fecha Ejecucion','N');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('FENR','SOLICITUD','Fecha Enrolamiento','N');


INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('CFAT','SOLICITUD','Clasificacion FATCA','N');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('IFAT','SOLICITUD','Indicador FATCA','N');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TP','SOLICITUD','Tipo Persona','N');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RIND','SOLICITUD','Indicios','N');


INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('MMIN','SOLICITUD','Monto Minimo','N');

INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('MMAX','SOLICITUD','Monto Maximo','N');




--DELETE FROM  "BSCL.Normativa.BD::NOR.TipoParametros" WHERE  "TipoParametro"='ZIPSAL';

---MASIVOS
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUT_MAS','SOLICITUD','RUT Masivo','S');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUTAUT_MAS','SOLICITUD','RUT Autorizado Masivo','S');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('RUTEMPL_MAS','SOLICITUD','RUT Empleado Masivo','S');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('NEMO_MAS','SOLICITUD','Nemo Masivo','S');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('CLAINS_MAS','SOLICITUD','Clasif. Inst. Masivo','S');

---OPCIONES TODOS
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_CLI','SOLICITUD','Todos los Clientes','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_EMP','SOLICITUD','Todos los Empleados','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_REL','SOLICITUD','Todos los Relacionados','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_AUT','SOLICITUD','Todos los Autorizados','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_NEMO','SOLICITUD','Todos los nemos','N');
INSERT INTO "BSCL.Normativa.BD::NOR.TipoParametros" ( "TipoParametro", "Entidad", "Nombre", "EsMasivo")
VALUES('TODOS_CLAINS','SOLICITUD','Todos Clas. Ins','N');

SET SCHEMA "BSCL";
/*==============================================================*/
/* Insert Table: ARCHIVOS NORMATIVA     					    */
/*==============================================================*/
SELECT * FROM  "BSCL.Normativa.BD::NOR.ArchivoNormativa"; 

--DELETE FROM  "BSCL.Normativa.BD::NOR.ArchivoNormativa" 
--WHERE "IdArchivoNormativa" in ('UAFDOFINFO-PDF')


INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('ART171CAR1-PDF','2016','ART171Car1','V1','ART171Car1','CARTA 1 ART. 171 BCS','Carta1Art171BCS','580002B2ED240F9EE10000007F000002','pdf','3580',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('ART171CAR2-PDF','2016','ART171Car2','V1','ART171Car2','CARTA 2 ART. 171 SVS','Carta2Art171SVS','5800B74AED240F9EE10000007F000002','pdf','3050',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CI63NOMINA-PDF','2016','CI63Nomina','V1','CI63Nomina','NOMINA DE PERSONAS','CI63NominaPersonas','580589A006BA0FB6E10000007F000002','pdf','3055',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CI63CARTA-PDF','2016','CI63Carta','V1','CI63Carta','CARTA DE RELACIONADOS','CI63Carta','5805BDB206BA0FB6E10000007F000002','pdf','3054',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('UAF48PEP-PDF','2016','UAF48PEP','V1','UAF48PEP','UAF CIR. 48 INFORME PEP','UAF48PEPINFO','5805D82821BE0FB6E10000007F000002','pdf','3056',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('UAFDOFCOMP-PDF','2016','UAFDOFCOMP','V1','UAFDOFCOMP','UAF COMPROBANTE DECLARACION ORIGEN DE FONDOS','UAFDOFCOMPINFO','','pdf','3077',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('UAFDOFINFO-PDF','2016','UAFDOF','V1','UAFDOF','UAF INFORME DECLARACION ORIGEN DE FONDOS','UAFDOFINFORME','','pdf','3076',1,'ACT');


---CIRCULARIZACION
INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CIRRESUMEN-PDF','2016','CIRCNomina','V1','CIRCNomina','RESUMEN DE CIRCULARIZACION','CIRRESUMEN','57FDB3A02AD70F97E10000007F000002','pdf','3039',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CIRCARTA-PDF','2016','CIRCCarta','V1','CIRCCarta','CARTA DE CIRCULARIZACION','CIRCERTIFICADOCUSTODIA','58073CB8DCC90FA2E10000007F000002','pdf','3063',1,'ACT');

--ANEXOS
INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('ANEXO4-PDF','2016','AMF','V1','AMF','ANEXO4 MONTOS FAC','ANEXO4','79267658EE477E13E1000000A1EE22E7','pdf','100004',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('ANEXO1-PDF','2016','ACPE','V1','ACPE','ANEXO1 Cta.Prop. y Especiales','ANEXO1','8EF676581F927E13E1000000A1EE22E7','pdf','100005',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('ANEXO13-PDF','2016','ACPOB','V1','ACPOB','ANEXO 13 OTRAS BOLSAS','ANEXO13','2ED686587209C913E1000000A1EE22E7','pdf','100006',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('EJECORD-PDF','2016','EJECORD','V1','EJECORD','EJECUCION DE ORDENES','EJECORD','','pdf','3236',1,'ACT');

INSERT INTO "BSCL.Normativa.BD::NOR.ArchivoNormativa" ("IdArchivoNormativa","VersionArchivo","Esquema","Version","IdTipoDocumento","Descripcion","NombreArchivo","IdArchivo","TipoArchivo","ServicioArchivo","IdEmpresa","IdEstado")
VALUES('CIR1985MAS-PDF','2016','CIR1985MAS','V1','CIR1985MAS','EJECUCION DE ORDENES PDF MASIVO','EJECORD','','pdf','3236',1,'ACT');


/*
---ARCHIVO TIPO CRYSTAL REPORTS
select * from BSCL."BSCL.Report::Template" WHERE "ReportName"='Carta1Art171BCS.rpt';
UPDATE BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" SET "IdArchivo"='57CF75DBFB000F1EE10000007F000002' WHERE "IdArchivoNormativa"='ART171CAR1-PDF';

select * from BSCL."BSCL.Report::Template" WHERE "ReportName"='Carta2Art171SVS.rpt';
UPDATE BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" SET "IdArchivo"='57CF75DAFB000F1EE10000007F000002' WHERE "IdArchivoNormativa"='ART171CAR2-PDF';

select * from BSCL."BSCL.Report::Template" WHERE "ReportName"='CI63NominaPersonas.rpt';
UPDATE "BSCL.Normativa.BD::NOR.ArchivoNormativa" SET "IdArchivo"='57CF79FAFB000F1EE10000007F000002' WHERE "IdArchivoNormativa"='CI63NOMINA-PDF';

select * from BSCL."BSCL.Report::Template" WHERE "ReportName"='CI63Carta.rpt';
UPDATE  BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" SET "IdArchivo"='57CF7AC1FB000F1EE10000007F000002' WHERE "IdArchivoNormativa"='CI63CARTA-PDF';

select * from BSCL."BSCL.Report::Template" WHERE "ReportName"='UAF48PEPINFO.rpt';
UPDATE  BSCL."BSCL.Normativa.BD::NOR.ArchivoNormativa" SET "IdArchivo"='57CF7AC2FB000F1EE10000007F000002' WHERE "IdArchivoNormativa"='UAF48PEP-PDF';

*/

/*==============================================================*/
/* Insert Table: AgregarAgenda           					    */
/*==============================================================*/
/*
--Insert Consolida Clientes en Agenda
SET SCHEMA "BSCL";
DROP TABLE #v_IdProgramTable;
CREATE LOCAL TEMPORARY TABLE #v_IdProgramTable
(
  "NombreParametro"   NVARCHAR(100),      
   "Valor"   NVARCHAR(100)
);

INSERT INTO #v_IdProgramTable VALUES ('IdEmpresa','1');
call "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda" (1,1,'NOR','ConsolidacionCli','N','**','*','**',8,30,'S','N','N','S',#v_IdProgramTable,?,?,?,?,?,?);

*/
/*==============================================================*/
/* Insert Table: Indicadores para Relacionados				    */
/*==============================================================*/


---- Indicadores Generales de NORMATIVO
insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (56, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Relacionado a la Corredora (Articulo 100)','RELCORR', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (57, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Acceso a Información Privilegiada (NCG 69)','INFPRV', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (58, 'CLIENTE', 1, 'N', 'CHECKBOX', 'Susceptible Conflicto de Interés','CONFINT', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

---- Indicadores por NORMATIVA
insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (60, 'CLIENTE', 1, 'N', 'COMBO', 'Tipo Relacion Corredora (CI 1064)','1064PROP', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

--DETALLE!
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,60,'PROPIEDAD','1 Vinculado por PROPIEDAD al Intermediario');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,60,'LABORAL','2 Vinculado LABORALMENTE al Intermediario');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,60,'ADMINISTRACION','3 Vinculado por ADMINISTRACION al Intermediario');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,60,'NOVINCULADO','4 NO VINCULADO');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,60,'NOINFORMAR','5 NO CORRESPONDE INFORMAR');

insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (61, 'CLIENTE', 1, 'N', 'COMBO', 'Cargo Relación con la Corredora (NCG SVS 69)','NCG69PROP', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

--DETALLE!
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,61,'DIRECTOR','1 DIRECTOR');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,61,'GERENTE','2 GERENTE');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,61,'ADMINISTRACION','3 ADMINISTRACION');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,61,'ASESOR','4 ASESOR');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,61,'OPERADOR','5 OPERADOR');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (6,61,'OTRO','6 OTRA PERSONA CON ACCESO A INFORMACION PRIVILEGIADA');	

insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (62, 'CLIENTE', 1, 'N', 'COMBO', 'Relación Corredora (NCG 380)','NCG380', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

--DETALLE!
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,62,'SOCIO','1 SOCIO');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,62,'DIRECTOR','2 DIRECTOR');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,62,'TRABAJADOR','3 TRABAJADOR');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,62,'MIEMBRO','4 MIEMBRO DEL GRUPO EMPRESARIAL');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,62,'OTRA','5 OTRA RELACION');
	
insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (63, 'CLIENTE', 1, 'N', 'COMBO', 'Tipo Relación Contraparte (DJ1820-DJ1829)','DJ1820', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );

--DETALLE!
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,63,'PROPIEDAD','1 Relación de Propiedad');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,63,'PARTICIPACION','2 Participación en la Dirección o Administración');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,63,'OTRO','3 Otros Tipos de Relación');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,63,'PARAISO','4 Contraparte Constituida en un País Considerado Paraíso Tributario');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (5,63,'NOEXISTE','5 No Hay Relación');
	
	
insert into bscl."BSCL.Plataforma.BD::IND.TipoIndicador" ( "IdTipoIndicador", "IdTipoEntidad",
  "IdEmpresa", "ValorIndicadorDefecto", "ClaseDato", "NombreTipoIndicador", "CodigoTipoIndicador", 
  "GrupoIndicador", "IdUso", "IdEstado", "CantidadDecimales", "LongitudMinima", "LongitudMaxima", "RequiereAutorizacion" ) 
VALUES (65, 'CLIENTE', 1, '', 'COMBO', 'Tipo Relación Corredora (CI 1237)','CI1237', 'NORMATIVOS', 'IND', 'ACT', 0, 1, 1, 'N' );
	
-DETALLE!
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (1,65,'GG','1 Gerente general');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (2,65,'GA','2 Gerente de área o subgerente');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (3,65,'OM','3  Operador de mesa de dinero');
	INSERT INTO bscl."BSCL.Plataforma.BD::IND.DetalleIndicador" ("IdDetalleIndicador","IdTipoIndicador","Codigo","DetalleIndicador")
	VALUES (4,65,'OR','4  Otra relación, distinta de las anteriores.');
	
--DELETE FROM bscl."BSCL.Plataforma.BD::IND.TipoIndicador" 	WHERE "IdTipoIndicador"=65


	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
