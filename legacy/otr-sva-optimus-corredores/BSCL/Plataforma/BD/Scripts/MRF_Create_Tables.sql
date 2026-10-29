--* -------------------------------------------------------------
--* Modulo ......: Plataforma
--* Sub.Modulo ..: Mesa RF
--* F. Creación .: Junio 2016
--* -------------------------------------------------------------
SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE	"BSCL.Plataforma.BD::PLA.Mesa" cascade;

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Mesa" (
	"IdMesa" 				BIGINT  		not null,
    "IdEmpresa" 			BIGINT			default '0' not null,
	"IdCliente" 			BIGINT			default '0' not null,
	"IdPortafolio" 			BIGINT			default '0' not null,
	"Codigo"				NVARCHAR(10)  	default ''  not null,		
	"Nombre" 				NVARCHAR(30)  	default ''  not null,	
	"IdTipoProducto" 		NVARCHAR(3)  	default ''  not null,-- RF, Spot, Forward	
	"IdEstadoMesa"			NVARCHAR(3) 	default '' 	not null,-- B : Bloqueada, C : Cerrada, A : Abierta
	"IdEstadoRegistro"		NVARCHAR(3) 	default '' 	not null,-- ACT : Activo, INA : Inactivo
	"IdUsuarioCreacion" 	BIGINT 			default '0' not null,
	"FechaCreacion" 		DATETIME		default '0001-01-01' not null, 	
PRIMARY KEY ("IdMesa") );	


/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
ALTER TABLE "BSCL.Plataforma.BD::PLA.Mesa"
	ADD CONSTRAINT 	"FK_Estado_Mesa" 
					FOREIGN KEY ("IdEstadoRegistro")
	REFERENCES 		"BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") 
					ON DELETE RESTRICT 
					ON UPDATE RESTRICT;

 

 