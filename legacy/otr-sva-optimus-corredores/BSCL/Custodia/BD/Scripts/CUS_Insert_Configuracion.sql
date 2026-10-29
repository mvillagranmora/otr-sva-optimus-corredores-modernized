SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:   BSCL.Custodia.BD::CUS.Configuracion         */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Custodia.BD::CUS.Configuracion"
("IdEmpresa", 
"CodigoParametro", 
"Descripcion", 
"Valor", 
"EsModificableUsuario", 
"Uso", 
"ValorDefecto", 
"ValoresPosibles", 
"ModificablePorUsuario", 
"ModificablePostProduccion", 
"CreadorParametro", 
"FechaCreacion", 
"ModificadorParametro", 
"FechaModificacion", 
"Observaciones")
VALUES(
1,'BLOQUEAR_VENTA_ACCIONES_CUSTODIA_ICLIENT','Bloquear la venta de custodia de acciones desde icliente','S','N','' ,'S',
'S/N' ,'N' ,'N' ,'',CURRENT_UTCTIMESTAMP,'' ,CURRENT_UTCTIMESTAMP,'');

/*==============================================================*/
/* ROLLBACK:          											*/
/*==============================================================*/

DELETE FROM BSCL."BSCL.Custodia.BD::CUS.Configuracion" 
WHERE "CodigoParametro" = 'BLOQUEAR_VENTA_ACCIONES_CUSTODIA_ICLIENT';

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;