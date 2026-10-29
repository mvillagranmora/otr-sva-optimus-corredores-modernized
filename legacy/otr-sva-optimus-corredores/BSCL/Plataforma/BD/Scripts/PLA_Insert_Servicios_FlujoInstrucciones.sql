SET SCHEMA "BSCL";

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/
INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700383, 'PLUGIN_ORD.jar@cl.bcs.ordenes.AceptaCancelacionInstruccionMasivo', 0, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700384, 'PLUGIN_ORD.jar@cl.bcs.ordenes.RechazaCancelacionInstruccionMasivo', 0, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(700385, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ConsultarSolInstruccionRechazada', 0, 'J', '', '', '', 'S');

INSERT INTO BSCL."BSCL.Plataforma.BD::PLA.Servicio"
("IdServicio", "RutaServicio", "UsoServicio", "TipoServicio", "Descripcion", "TablasTempIN", "TablasTempOUT", "RegistraUso")
VALUES(900201, 'PLUGIN_ORD.jar@cl.bcs.ordenes.ProcesarCargaMasivaInstruccion', 0, 'J', '', '', '', 'S');


/*==============================================================*/
/* ROLLBACK:                                                    */
/*==============================================================*/
/*
DELETE FROM  BSCL."BSCL.Plataforma.BD::PLA.Servicio"
WHERE "IdServicio" IN (700383,700384,700385,900201); 
*/

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;