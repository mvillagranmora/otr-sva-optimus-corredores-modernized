
SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/


ALTER TABLE BSCL."BSCL.Orden.BD::CAR.CargaMasivaDetalle" ADD ("Tasa" DECIMAL(28,6) DEFAULT 0);

INSERT INTO BSCL."BSCL.Orden.BD::CAR.TipoCargaMasiva" 
("IdTipoCargaMasiva", "CodigoTipoCargaMasiva", "Nombre", "IdEmpresa", "IdEstado")
VALUES(2, 'CARGA_INGRESO_INSTRUCCIONES_MASIVA', 'INGRESO INSTRUCCIONES MASIVO', 1, 'ACT');



/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM  "BSCL.Orden.BD::CAR.TipoCargaMasiva" 
WHERE "CodigoTipoCargaMasiva" = 'CARGA_INGRESO_INSTRUCCIONES_MASIVA'; 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;


