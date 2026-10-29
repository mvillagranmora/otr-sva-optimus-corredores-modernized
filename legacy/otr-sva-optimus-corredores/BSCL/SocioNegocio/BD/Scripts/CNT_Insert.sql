SET SCHEMA "BSCL";

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/

DELETE FROM  "BSCL.SocioNegocio.BD::CNT.Contraparte"; 

/*==============================================================*/
/* Insert Table:                                                */
/*==============================================================*/

INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte" ( "IdContraparte","IdPersona","Codigo","Emisor","Depositos","Bolsa","Corredora","CamaraDeCompensacion","Banco","IdEstado","IdEmpresa" )  
SELECT 100, PER."IdPersona" ,'51','N','N','S','S','N','N','ACT',1
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER where trim(PER."DocumentoIdentidad") = trim('96519800-8');

INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte" ( "IdContraparte","IdPersona","Codigo","Emisor","Depositos","Bolsa","Corredora","CamaraDeCompensacion","Banco","IdEstado","IdEmpresa"  )  
SELECT 101, PER."IdPersona" ,'51','N','N','S','S','N','N','ACT',1
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER where trim(PER."DocumentoIdentidad") = trim('96564330-3');

INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte" ( "IdContraparte","IdPersona","Codigo","Emisor","Depositos","Bolsa","Corredora","CamaraDeCompensacion","Banco","IdEstado","IdEmpresa"  )  
SELECT 102, PER."IdPersona" ,'51','N','N','S','S','N','N','ACT',1
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER where trim(PER."DocumentoIdentidad") = trim('96535720-3');

INSERT INTO "BSCL.SocioNegocio.BD::CNT.Contraparte" ( "IdContraparte","IdPersona","Codigo","Emisor","Depositos","Bolsa","Corredora","CamaraDeCompensacion","Banco","IdEstado","IdEmpresa"  )  
SELECT 103, PER."IdPersona" ,'51','N','N','S','S','N','N','ACT',1
FROM "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersona" as PER where trim(PER."DocumentoIdentidad") = trim('96571220-8');

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
