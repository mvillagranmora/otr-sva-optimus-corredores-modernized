SET SCHEMA "BSCL";
	
/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Plataforma.BD::CTT.ParametroContratos" cascade;
drop table "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
create column table "BSCL.Plataforma.BD::CTT.ContratoDatosGenerales" (
   "IdEmpresa"         		BIGINT              null, 
   "RazonSocial"     		NVARCHAR(200)       null,
   "RUT"	          		NVARCHAR(40)        null,
   "Direccion"			 	NVARCHAR(200)		null,
   "Telefono" 			 	NVARCHAR(20)		null,
   "Email" 					NVARCHAR(100)		null,
   "Fax"					NVARCHAR(20) 		null,
   "Texto1"					NVARCHAR(1000)		null,
   "Texto2"					NVARCHAR(1000) 		null,
   --Agregar Blob para logos...
	"LogoPrincipal"			BLOB				null,
	"LogoSecundario"		BLOB				null,
   primary key ("IdEmpresa")
);


create column table "BSCL"."BSCL.Plataforma.BD::CTT.ParametroContratos" (
   "IdParametroContrato" bigint			not null,
   "CodigoArchivo"       NVARCHAR(50)   not null,
   "Nombre"              NVARCHAR(200)  not null,
   "Tipo"           	 NVARCHAR(20)   not null,
   primary key ("IdParametroContrato")
);

         

/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */


      
/*==============================================================*/
/* SECUENCES:                                                       */
/*==============================================================*/


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */



/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit;
