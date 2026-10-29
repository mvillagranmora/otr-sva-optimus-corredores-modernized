
--- REGULARIZACION LIBERACIONES

--- FECHA : [2022-12-20]
--tabla de instrumentos art 104

SET SCHEMA "BSCL";

DROP TABLE BSCL."BSCL.Normativa.BD::TRI.InstrumentosArticulo104";

CREATE TABLE BSCL."BSCL.Normativa.BD::TRI.InstrumentosArticulo104" (
	"Periodo" 		BIGINT NOT NULL,
	"Instrumento" 	NVARCHAR(40) default '' 	not null,
	"Emisor" 		NVARCHAR(40) default '' 	not null,
	"Moneda" 		NVARCHAR(10) default '' 	not null,
	"Usuario" 		NVARCHAR(10) default '' 	not null,
	"Tasa_Facial" 	DECIMAL(26,8) default 0 	not null,
	"Fecha_Act"  	DATE DEFAULT '0001-01-01' 	not null
);
	


alter table BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874" drop ("IdOperacion");

-- Se agregan campos de valorizacion a la DJ 1874


ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD ("IdClasificacionTipoInstrumento" 			NVARCHAR(3)		default '' 	not null,
	 "IdTipoInstrumento" 						NVARCHAR(3) 	null,--*
	 "IdInstrumento"                    		BIGINT 			default 0 not null,
	 "InsFechaVencimiento" 						DATE            default '0001-01-01' not null,
	 "InsFechaEmision" 							DATE            default '0001-01-01' not null,
	 "IdMonedaInstrumento"						NVARCHAR(3) 	null,--*
	 "IdMonedaEmisionInstrumento"				NVARCHAR(3) 	null,--*
	 --"IdTipoReajusteInstrumento" 				NVARCHAR(3) 	default ''	not NULL, 
	 "IdEmisor" 								NVARCHAR(40)	default '' 	not null,
	 --"IdCondicionDeLiquidacion"					NVARCHAR(2) 	default ''  NOT NULL,
	 "FechaLiquidacion" 						DATE 			DEFAULT '0001-01-01'NOT NULL,
	 "Cantidad" 				     		    DECIMAL(26,8)  	NOT NULL DEFAULT 0,  
	 "Tasa"										DECIMAL(26,8)	NULL,
	 "Precio"									DECIMAL(26,8)	NULL
	);
	

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD (
 	 "ValIndicadorValorizado"					NVARCHAR(3) 	default 'NO' null,
	 "ValMontoMonedaInstrumento"				DECIMAL(26,8) DEFAULT NULL,
	 "ValMontoDevengadoAMonedaInstrumento"		DECIMAL(26,8) DEFAULT NULL,
	 "ValMontoMonedaSistema"					DECIMAL(26,8) DEFAULT NULL,
	 "ValMontoDevengadoAMonedaSistema"			DECIMAL(26,8) DEFAULT NULL
	);	
	
	
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD (
 	 "ValMontoMonedaInstrumentoFechaVenta"				DECIMAL(26,8) DEFAULT NULL,
	 "ValMontoMonedaSistemaFechaVenta"					DECIMAL(26,8) DEFAULT NULL
	);
	
ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD (	 "FolioCartera"                    		BIGINT 			default 0 not null);


ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD (	 "FechaAdquisicion" 						DATE 			DEFAULT '0001-01-01'NOT NULL);

ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD ("ValResultado" 			NVARCHAR(400)		default ''  null);


ALTER TABLE BSCL."BSCL.Normativa.BD::TRI.DatosDJ1874"
ADD ("Nemotecnico" 									NVARCHAR(40)	default '' 	not null--,
	 --"ValorRescate"									DECIMAL(26,8)
	);