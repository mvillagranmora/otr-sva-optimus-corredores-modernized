SET SCHEMA "BSCL";



DROP TABLE "BSCL.Normativa.BD::NOR.DatosCI1720";
/*==============================================================*/
/* Create Table:  "BSCL.Normativa.BD::NOR.DatosCI1720"          */
/*==============================================================*/
/*

*/
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.DatosCI1720"(

	"ID" 						BIGINT NOT NULL,
	"IdDocumento" 				BIGINT NOT NULL,
	
	-- VoBo del usuario sobre el registro (S/N).
    "VboUsuario" 				char(1) not null,
    -- Observacion ingresada por usuario que edito el registro.
    "Observaciones" 			nvarchar(250) not null,
    -- INICIAL / EXTERNA.
    "FuenteDatos" 				nvarchar(40) not null,

	"Nemotecnico" 				NVARCHAR(20) NOT NULL,
	"NombreEmisor" 				NVARCHAR(100) NOT NULL,
	"TipoInstrumento" 			NVARCHAR(10) NOT NULL,
	"CantidadUnidadesNominal" 	DECIMAL(15,4) NOT NULL,
	"TipoUnidades" 				VARCHAR(15) NOT NULL,
	"Valorizacion" 				BIGINT NOT NULL,
	"CarteraPropia" 			DECIMAL(15,4) NOT NULL,
	"CustodiaTerceros" 			DECIMAL(15,4) NOT NULL,
	"CustodiaPropiaDCV" 		DECIMAL(15,4) NOT NULL,
	"CustodiaTercerosDCV" 		DECIMAL(15,4) NOT NULL,
	"CustodiaSociedadEmisora" 	DECIMAL(15,4) NOT NULL,
	"CustodiaIntermediario" 	DECIMAL(15,4) NOT NULL,
	"CustodiaBolsas" 			DECIMAL(15,4) NOT NULL,
	"CustodiaOtros" 			DECIMAL(15,4) NOT NULL,
	"FechaRegistro"				DATE NULL,
	"Usuario"					NVARCHAR(30) NULL,
	
	PRIMARY KEY ("ID")
);

alter table "BSCL.Normativa.BD::NOR.DatosCI1720"
    add constraint "FK_Documento_DatosCI1720" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete restrict on update restrict;
        
/* alter table "BSCL.Normativa.BD::NOR.DatosCI1720"
    add constraint "FK_SolicitudCircular_DatosCI1720" foreign key ("IdSolicitudCircular")
        references "BSCL.Normativa.BD::NOR.SolicitudCircular" ("IdSolicitudCircular")
        on delete restrict on update restrict; */


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
