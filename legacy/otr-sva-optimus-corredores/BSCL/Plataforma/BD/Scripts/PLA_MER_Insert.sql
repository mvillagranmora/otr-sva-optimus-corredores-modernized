SET SCHEMA "BSCL";




--PARIDAD TIPO CAMBIO
delete from  "BSCL.Plataforma.BD::MER.ParidadYTipoCambio";;
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-15' as "Fecha",'USD' AS "IdMoneda",'OBSERVADO' AS "TipoRegistro",659 AS "TipoCambioMonedaLocal", 1 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-15' as "Fecha",'USD' AS "IdMoneda",'CORREDORA' AS "TipoRegistro",657 AS "TipoCambioMonedaLocal", 1 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-15' as "Fecha",'USD' AS "IdMoneda",'MANUAL' AS "TipoRegistro",658 AS "TipoCambioMonedaLocal", 1 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-14' as "Fecha",'USD' AS "IdMoneda",'MANUAL' AS "TipoRegistro",654 AS "TipoCambioMonedaLocal", 1 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-13' as "Fecha",'USD' AS "IdMoneda",'CENTRAL' AS "TipoRegistro",653 AS "TipoCambioMonedaLocal", 1 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-15' as "Fecha",'YEN' AS "IdMoneda",'CENTRAL' AS "TipoRegistro",0 AS "TipoCambioMonedaLocal", 115.0700 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	
INSERT INTO "BSCL.Plataforma.BD::MER.ParidadYTipoCambio"("IdEmpresa","Fecha","IdMoneda","TipoRegistro","TipoCambioMonedaLocal","ParidadMonedaCambio","IdEstado")
SELECT 1 as "IdEmpresa",'2016-12-16' as "Fecha",'EUR' AS "IdMoneda",'CENTRAL' AS "TipoRegistro",0 AS "TipoCambioMonedaLocal", 0.9624 AS "ParidadMonedaCambio",'ACT' as "IdEstado" FROM DUMMY;	


--UsoTipoRegistroParidadYTipoCambio
delete from  "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio";
INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'COMPRA MILA' AS "IdUso",1 AS "Prioridad",'MANUAL' AS "TipoRegistro",'S' AS "AplicaOrigenCambio",'S' AS "AplicaCambioLocal",'N' AS "AplicaLocalCambio",'N' AS "AplicaCambioDestino" FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'COMPRA MILA' AS "IdUso",2 AS "Prioridad",'OBSERVADO' AS "TipoRegistro",'N' AS "AplicaOrigenCambio",'S' AS "AplicaCambioLocal",'N' AS "AplicaLocalCambio",'N' AS "AplicaCambioDestino" FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'COMPRA MILA' AS "IdUso",3 AS "Prioridad",'MESA' AS "TipoRegistro",'N' AS "AplicaOrigenCambio",'N' AS "AplicaCambioLocal",'S' AS "AplicaLocalCambio",'N' AS "AplicaCambioDestino" FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'COMPRA MILA' AS "IdUso",4 AS "Prioridad",'CENTRAL' AS "TipoRegistro",'S' AS "AplicaOrigenCambio",'S' AS "AplicaCambioLocal",'S' AS "AplicaLocalCambio",'S' AS "AplicaCambioDestino" FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'VENTA MILA' AS "IdUso",1 AS "Prioridad",'MESA' AS "TipoRegistro",'N' AS "AplicaOrigenCambio",'N' AS "AplicaCambioLocal",'S' AS "AplicaLocalCambio",'N' AS "AplicaCambioDestino" FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::MER.UsoTipoRegistroParidadYTipoCambio" 
("IdEmpresa","IdUso","Prioridad","TipoRegistro","AplicaOrigenCambio","AplicaCambioLocal","AplicaLocalCambio","AplicaCambioDestino")
SELECT 1 AS "IdEmpresa",'VENTA MILA' AS "IdUso",2 AS "Prioridad",'MANUAL' AS "TipoRegistro",'N' AS "AplicaOrigenCambio",'N' AS "AplicaCambioLocal",'S' AS "AplicaLocalCambio",'N' AS "AplicaCambioDestino" FROM DUMMY;





--USO PARIDAD Y TIPO CAMBIO
delete from  "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio";
INSERT INTO "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio"("IdEmpresa","IdUso","NombreUso","DescripcionUso","TipoBusqueda")
select 1 as "IdEmpresa",'COMPRA MILA' AS "IdUso",'COMPRA MILA' AS "NombreUso",'COMPRA MILA' AS "DescripcionUso",'D' AS "TipoBusqueda" from dummy;
INSERT INTO "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio"("IdEmpresa","IdUso","NombreUso","DescripcionUso","TipoBusqueda")
select 1 as "IdEmpresa",'VENTA MILA' AS "IdUso",'VENTA MILA' AS "NombreUso",'VENTA MILA' AS "DescripcionUso",'U' AS "TipoBusqueda" from dummy;
INSERT INTO "BSCL.Plataforma.BD::MER.UsoParidadYTipoCambio"("IdEmpresa","IdUso","NombreUso","DescripcionUso","TipoBusqueda")
select 1 as "IdEmpresa",'VENTA EXTRANJEROS' AS "IdUso",'VENTA EXTRANJEROS' AS "NombreUso",'VENTA EXTRANJEROS' AS "DescripcionUso",'D' AS "TipoBusqueda" from dummy;

--PARAEMTROS FINANCIEROS
delete from "BSCL.Plataforma.BD::MER.ParametroFinanciero";

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UF','2016-01-01','','','DI','UNIDAD DE FOMENTO','UNIDAD DE FOMENTO',24000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UF','2016-01-02','','','DI','UNIDAD DE FOMENTO','UNIDAD DE FOMENTO',25000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UF','2016-01-03','','','DI','UNIDAD DE FOMENTO','UNIDAD DE FOMENTO',26000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UTM','','2016-01','','ME','UNIDAD TRIBUTARIA MENSUAL','UNIDAD TRIBUTARIA MENSUAL',33000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UTM','','2016-02','','ME','UNIDAD TRIBUTARIA MENSUAL','UNIDAD TRIBUTARIA MENSUAL',35000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UTM','','2016-03','','ME','UNIDAD TRIBUTARIA MENSUAL','UNIDAD TRIBUTARIA MENSUAL',46000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

INSERT INTO "BSCL.Plataforma.BD::MER.ParametroFinanciero"("IdParametroFinanciero","Fecha","AgnoMes","Agno","Periodo","Nombre","Descripcion","Valor","IdMoneda","FechaActualizacion")
SELECT 'UTM','','2016-04','','ME','UNIDAD TRIBUTARIA MENSUAL','UNIDAD TRIBUTARIA MENSUAL',33000,'CLP',CURRENT_TIMESTAMP FROM DUMMY;	

--TIPO REGISTRO PARIDAD
INSERT INTO  "BSCL.Plataforma.BD::MER.TipoRegistoParidad"
SELECT 'CORREDORA','CORREDORA' FROM DUMMY

INSERT INTO  "BSCL.Plataforma.BD::MER.TipoRegistoParidad"
SELECT 'OBSERVADO','OBSERVADO' FROM DUMMY

INSERT INTO  "BSCL.Plataforma.BD::MER.TipoRegistoParidad"
SELECT 'COMPRA_MILA','COMPRA_MILA' FROM DUMMY

INSERT INTO  "BSCL.Plataforma.BD::MER.TipoRegistoParidad"
SELECT 'VENTA_MILA','VENTA_MILA' FROM DUMMY


INSERT INTO  "BSCL.Plataforma.BD::MER.TipoRegistoParidad"
SELECT 'CENTRAL','CENTRAL' FROM DUMMY

commit;
rollback
