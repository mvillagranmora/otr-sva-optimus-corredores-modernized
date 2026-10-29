SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700379, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarInstruccionMotivo', NULL, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700380, 'PLUGIN_ORD.jar@cl.bcs.ordenes.CrearInstruccionMotivo', NULL, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700381, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ModificarInstruccionMotivo', NULL, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700382, 'BSCL.Orden.BD.Vistas::ConsultarMotivosInstruccion', 0, 'Q', '', '', '', 'S');


/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Servicio"
WHERE "IdServicio" IN (700379,700380,700381,700382); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;