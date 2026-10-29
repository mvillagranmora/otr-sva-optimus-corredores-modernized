SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Tables                                                */
/*==============================================================*/
DELETE FROM "BSCL.Normativa.BD::UAF.MedioDePagoDOF";



INSERT INTO "BSCL.Normativa.BD::UAF.MedioDePagoDOF" ("IdEmpresa", "IdMedioDePago", "IdEstado")
VALUES (1,'EFE','ACT');



--SELECT * FROM "BSCL.Plataforma.BD::PAG.MedioDePago"
	
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
