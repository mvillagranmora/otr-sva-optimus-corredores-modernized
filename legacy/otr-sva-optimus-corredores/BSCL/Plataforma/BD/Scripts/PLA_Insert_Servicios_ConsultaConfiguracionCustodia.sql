SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700710, 'BSCL.Custodia.BD.Vistas::CUS.ConsultaConfiguracionCustodia', 0, 'Q', '', '', '', 'S');

/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio"
WHERE "IdServicio" IN (700710); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;