SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Normativa.BD::CER.CertificadoCliente" CASCADE;




/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

 
CREATE COLUMN TABLE "BSCL.Normativa.BD::CER.CertificadoCliente"(
"IdCertificadoCliente" BIGINT NOT NULL,
"IdTipoDocumento" 	NVARCHAR(10) NOT NULL,
"IdEmpresa" 		BIGINT NOT NULL,
"Esquema" 			NVARCHAR(10) NOT NULL,
"Anio" 				NVARCHAR(4) NOT NULL,
"Periodo" 			NVARCHAR(6) NOT NULL,
"IdCliente" 		BIGINT NOT NULL DEFAULT 0,
"IdPortafolio" 		BIGINT NOT NULL DEFAULT 0,
"Version" 			NVARCHAR(10) NOT NULL,
"JsonData" 			BLOB NOT NULL,
PRIMARY KEY ("IdCertificadoCliente")
);
		
	    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/


drop sequence "BSCL.Normativa.BD.Secuencias::IdCertificadoCliente";
create sequence "BSCL.Normativa.BD.Secuencias::IdCertificadoCliente" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit





