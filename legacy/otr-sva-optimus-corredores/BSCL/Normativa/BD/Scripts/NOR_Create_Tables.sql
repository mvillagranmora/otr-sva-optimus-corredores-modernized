SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
drop table "BSCL.Normativa.BD::NOR.EstadoNormativa" 				CASCADE;

drop table "BSCL.Normativa.BD::NOR.Configuracion" 					CASCADE;

drop table "BSCL.Normativa.BD::NOR.SolicitudCircular" 				CASCADE;

drop table "BSCL.Normativa.BD::NOR.Normativa"						CASCADE;

drop table "BSCL.Normativa.BD::NOR.TipoCircular" 					CASCADE;

drop table "BSCL.Normativa.BD::NOR.SolicitudParametros" 			CASCADE; 

drop table "BSCL.Normativa.BD::NOR.TipoParametros" 					CASCADE; 

drop table "BSCL.Normativa.BD::NOR.Producto" 						CASCADE;
drop table "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion" 		CASCADE;
drop table "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion"  CASCADE;
drop table "BSCL.Normativa.BD::NOR.NormativaRelacion" 				CASCADE;
drop table "BSCL.Normativa.BD::NOR.ArchivoNormativa" 				CASCADE;
drop table "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente"  CASCADE;

drop table "BSCL.Normativa.BD::NOR.LogNormativa" 					CASCADE;
drop table "BSCL.Normativa.BD::NOR.Indicador"  						CASCADE;
drop table "BSCL.Normativa.BD::NOR.TipoLogNormativa"  				CASCADE;


drop table "BSCL.Normativa.BD::NOR.ClienteFatca" 					CASCADE;

drop table "BSCL.Normativa.BD::NOR.DatosFatca" 						CASCADE;

drop table "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1922" 				CASCADE;



CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.EstadoNormativa" (
 "IdEstadoNormativa" 		BIGINT  	 	NOT NULL ,
 "IdEstado" 				NVARCHAR(40) 	NOT NULL ,
 "NombreEntidad"			NVARCHAR(100) 	NOT NULL , 
 PRIMARY KEY ("IdEstadoNormativa"));


/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


create column table "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1922" (
	----------------------------------------
	---- CAMPOS COMUNES EN TODAS LAS DJS
	----------------------------------------
    "ID" bigint not null,	    				-- Id del registro. Por convencion, en todas las tablas de este tipo el nombre de esta columna debe ser Id.
    "IdDocumento" bigint not null, 				-- Id del Documento.
    "VboUsuario" char(1) not null, 				-- VoBo del usuario sobre el registro (S/N).
    "Observaciones" nvarchar(250) not null,     -- Observacion ingresada por usuario que edito el registro.
    "FuenteDatos" nvarchar(40) not null, 		-- INICIAL / EXTERNA.
    "IdCliente" bigint null,     				-- Identificador unico de cliente del registro Base Cliente.
    "IdPortafolio" bigint null,					-- Identificador unico de Portafolio del registro Base Cliente.

	----------------------------------------
	---- Especificos para la DJ: Datos de los informados
	----------------------------------------
    "RutAdministradora" 	nvarchar(40) 	not null,
    "RutFondo" 				nvarchar(40) 	not null,    
    "Nemotecnico" 			nvarchar(100) 	null	,    	-- Nemotecnico del instrumento.
    "TipoFondo"   			nvarchar(1) 	not null,  		-- Diccionario y Homologacion Tipo Instrumento, Tipo de Fondo Mutuo, de Inversion Privado, Publico
	"NombreParticipe" 		nvarchar(80)	not null,		--   
    "RutCliente" 			nvarchar(40) null, 			-- RUT del cliente. Nota: Existen RUT de 9 digitos, 100.000.000-1, no los tolera el SII, pero deben registrar operacion, aunque la validacion
	"TaxId" 				nvarchar(15) 	not null,	
	"CodigoPais"			nvarchar(15) 	not null, -- Homologar, DJ requiere sigla
	"TipoOperacion" 		integer  		NOT NULL, -- Diccionario, Criterio para Cuadrar

	-- SECCIÓN B1: INFORMACIÓN DE LOS APORTES AL FONDO
	"FechaInversion" 				date 			not null,	
	"MonedaReajuste"    			integer 		not null,-- Homologar y Diccionario, DJ requiere sigla
 	"MontoNominalAportes" 			bigint 	not null,
 	"NumeroSerieCuotas" 			Integer 		not null,
  	"CantidadCuotasParticipe" 		bigint 	not null,
  	"ParticipeCumpleRequisitos"		Integer 		not null, -- Diccionario
  	"CuotasCumplenRequisitos"		Integer 		not null, -- Diccionario
	"ValorCuotaPesos" 				decimal(18, 2)  not null, -- Diccionario
	"PorcentajeParticipacion31Dic" 	decimal(5, 2) 	not null, -- Diccionario

	-- SECCIÓN B2: DETALLE DE OPERACIONES Y MOVIMIENTOS
 	"ValNominalCuotasRescatadas"	bigint 	not null,
 	"CantidadCuotasRescatadas"		bigint 	not null,
	"DiferenciaPorRescateOEnajenacion"	bigint 	not null,
	"DifPorRescateOEnajenacionCFIAcogido107"	bigint 	not null,
 	"ValNominalInversionaFechaLiquidacion"	bigint 	not null,
 	"CantCuotasCedidasPorLiquidacion"	bigint 	not null,	
	"DiferenciaValorCuotasFechaLiquidacion"	bigint 	not null,
		 	
	-- SECCIÓN B3: MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)		 	
 	"DividendosAfectosIGCoAdicional"			bigint 	not null,
 	"DividendosExentosIGCoAdicional"			bigint 	not null,
 	"DividendosNoConstitutivosDeRenta"			bigint 	not null,
 	"DividendosAfectosImptoUnicoTasa10"		bigint 	not null,
 	"DividendosNoAfectosImptoUnicoTasa10"	bigint 	not null,
 	"DividendosAfectosImptoUnicoTasa4"		bigint 	not null,
 	"DistribucionXDiminucionNoImputadaCapital"	bigint 	not null,
 	"DevolucionesDeCapital"						bigint 	not null,
	"IncrementoPorImpuesto"						bigint 	not null,

	-- SUBSECCIÓN B4: CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL		 	
 	"CreditoImpto1CatConDevolucionAfectas"		bigint 	not null,
 	"CreditoImpto1CatSinDevolucionAfectas"		bigint 	not null,
 	"CreditoImpto1CatConDevolucionExentas"		bigint 	not null,
 	"CreditoImpto1CatSinDevolucionExentas"		bigint 	not null,
 	
 	"CreditoPorImpuestosExternos"					bigint 	not null,
 	"ImpuestosTasaAdicionalExArticulo21"			bigint 	not null,

 	"MontoInversionenCuotasCFIAnterior93"		bigint 	not null,
 	"NumeroCertificado"							Integer 	not null,

	-----------------------------------------------------------------------------------
    -- Interpretación respecto al tipo Operacion Original. Datos utiles para cuadratura
	-----------------------------------------------------------------------------------
	-- Para Cuadrar Dividendos
    "IdInstrumento"             bigint not null,
    "IdEventoCapitalCliente"    bigint not null,

    -- CLT, COR, CIA, BOL, CAM, CUS
    "TipoSocioNegocio"          NVARCHAR(1) not null,
    "TipoCartera"               NVARCHAR(3) not null,
    "IdMoneda"                  NVARCHAR(3) not null,
    "TipoEventoCapital"         NVARCHAR(3) not null,

    primary key ("ID")
);

alter table "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1922"
add constraint "FK_Documento_DatosDJ1922" foreign key ("IdDocumento")
references "BSCL"."BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
on delete restrict on update restrict;
        







CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.Configuracion" (
	 "IdEmpresa" 		BIGINT CS_FIXED NOT NULL ,
	 "CodigoParametro" 	NVARCHAR(40) 	NOT NULL ,
	 "Descripcion" 		NVARCHAR(520) 	NOT NULL ,
	 "Valor" 			NVARCHAR(520) 	NOT NULL , 
	 "IdNormativa"      NVARCHAR(15)    NOT NULL  DEFAULT '',
 	 PRIMARY KEY ("IdEmpresa", "CodigoParametro")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.SolicitudParametros" (
 "IdSolicitudParametros"	BIGINT 		  NOT NULL ,
 "IdSolicitudCircular" 		BIGINT 		  NOT NULL,
 "TipoParametro"                NVARCHAR(100) NOT NULL,
 "Valor" 		        NVARCHAR(100) NOT NULL,
 PRIMARY KEY ("IdSolicitudParametros")) ;


CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.TipoParametros" (
 "TipoParametro"                NVARCHAR(100) NOT NULL,
 "Entidad"		 		        NVARCHAR(40) NOT NULL DEFAULT '',
 "Nombre"	 		        	NVARCHAR(100) NOT NULL DEFAULT '',
 "EsMasivo"		 		        NVARCHAR(2) NOT NULL DEFAULT 'N',
 "EsInterno"		 		    NVARCHAR(2) NOT NULL DEFAULT 'N',
 PRIMARY KEY ("TipoParametro","Entidad"));
 

--drop table "BSCL"."BSCL.Normativa.BD::NOR.SolicitudCircular"
--ALTER TABLE "BSCL.Normativa.BD::NOR.SolicitudCircular" ADD ("EsPauta" NVARCHAR(10) default 'N');

CREATE COLUMN TABLE "BSCL"."BSCL.Normativa.BD::NOR.SolicitudCircular"(


"IdSolicitudCircular"	    BIGINT		NOT NULL,

--grales

"IdTipoCircular"		    NVARCHAR(100)		NOT NULL,
"FechaIngresoSolicitud"		DATE				NULL,
"FechaVencimientoSolicitud"	DATE				NULL,
"IdUsuario"					BIGINT				NOT NULL,
--"IdEmpresa"				BIGINT				NOT NULL,
"EstadoSolicitud"			NVARCHAR(100)		DEFAULT ''	NULL,
"Observaciones"				NVARCHAR(100)		DEFAULT ''	NULL,
"IdTipoDocumento"			NVARCHAR(100)		DEFAULT ''	NULL,
"IdDocumento"  				BIGINT 				NOT NULL,

--filtros 
	
"IdCliente"         		BIGINT				NULL,
"FechaInicio"		    	DATE				NULL,
"FechaFin"		        	DATE				NULL,
"ListaRut"              	NVARCHAR(10)		NULL,
"ListaRutAut"           	NVARCHAR(10)		NULL,
"ListaNemo"             	NVARCHAR(10)		NULL,
"ListaRutEmpl"             	NVARCHAR(10)		NULL,
"Periodicidad"				NVARCHAR(100)		NULL,

-- resultantes

"RutaArchivo"				NVARCHAR(200)    	NULL,
"NombreArchivo"				NVARCHAR(200)    	NULL,
"NumeroRegistros"			NVARCHAR(200)    	NULL,
"IdProceso"         		BIGINT				NULL,
"EsPauta"			        NVARCHAR(10)    	NULL,
"RutaLog"			    	NVARCHAR(200)    	NULL,
"NumeroRegistrosLog"		NVARCHAR(200)    	DEFAULT '0' NULL,

--Auditoria
 "FechaIngreso"       		SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
 "FechaCreacion"     		SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
 "FechaModificacion"  		SECONDDATE      DEFAULT '0001-01-01 00:00:00' NOT NULL,
 "IdEstado" 				NVARCHAR(3) 	DEFAULT 'PEN' 				  NOT NULL ,
 "Borrado" 					NVARCHAR(1) 	DEFAULT 'N',


PRIMARY KEY ("IdSolicitudCircular") );



CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.Normativa" (
 "IdNormativa"              NVARCHAR(15)            not null,
 "NombreNormativa"          NVARCHAR(100)           DEFAULT ''  not null,
 "IdEstado"                 NVARCHAR(3)             DEFAULT 'ACT' not null,
 "MetodoRelacionado"        NVARCHAR(15)            DEFAULT 'DIRECTA' not null,
 "EntidadDestino"           NVARCHAR(20)            DEFAULT '' not null,
 "NombreDestinatario"      NVARCHAR(100)    		NOT NULL  DEFAULT '',
 "CargoDestinatario"       NVARCHAR(40)    			NOT NULL  DEFAULT '',
 "UI_Emision" 	  			NVARCHAR(2)     NOT NULL  DEFAULT 'S',
  PRIMARY KEY ("IdNormativa"))  ;

  
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.Producto" (
 "IdNormativa"              		NVARCHAR(15)            	not null,
 "IdTipoDocumento" 					NVARCHAR(10) 	NOT NULL DEFAULT '',
 "IdTipoProducto" 					NVARCHAR(4) 	DEFAULT '' 	not null,
 "IdClasificacionTipoInstrumento"	NVARCHAR(4) 	DEFAULT ''	not null,
 "IdTipoInstrumento"  		 		NVARCHAR(3)   	DEFAULT ''	not null,
 "IdTipoOperacion"					NVARCHAR(6)   	DEFAULT ''	not null,
 "IdEstado"                 		NVARCHAR(3)     DEFAULT 'ACT' not null,
 "IdConcepto"        				NVARCHAR(6)     DEFAULT ''	not null,
 "Uso"         						NVARCHAR(10)    DEFAULT ''	not null,
 "Origen"          					NVARCHAR(10)    DEFAULT ''	not null,
 "HIdTipoProducto" 					NVARCHAR(4) 	DEFAULT '' 	not null, -- Homologo de Normativa
 "HIdClasificacionTipoInstrumento"	NVARCHAR(4) 	DEFAULT ''	not null,
 "HIdTipoInstrumento"  		 		NVARCHAR(3)   	DEFAULT ''	not null,
 "HIdTipoOperacion"					NVARCHAR(6)   	DEFAULT ''	not null,
  PRIMARY KEY ("IdNormativa","IdTipoDocumento", "IdTipoProducto", "IdClasificacionTipoInstrumento", "IdTipoInstrumento","IdTipoOperacion","Uso"));



--Homologacion Normativa Relacionados

--Agrupador de Relaciones MetodoRelacionado:Clasificacion
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.TipoClasificacionRelacion" (
 "IdClasificacionRelacion"	NVARCHAR(20)            NOT NULL,
 "IdNormativa"              NVARCHAR(15)            NOT NULL,
 "CodigoRelacionNorma"      NVARCHAR(10)            DEFAULT ''  NOT NULL,
 "DescripcionRelacionNorma" NVARCHAR(100)           DEFAULT ''  NOT NULL,
 "IdEstado"                 NVARCHAR(3)             DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdClasificacionRelacion"));
    
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.NormativaRelacionClasificacion" (
 "IdNormativaRelacionClasificacion"		BIGINT					NOT NULL,
 "IdClasificacionRelacion"				NVARCHAR(20)            NOT NULL,
 "IdTipoRelacion"						NVARCHAR(6)				DEFAULT '' 	NOT NULL,
  "IdEstado"                			NVARCHAR(3)             DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdNormativaRelacionClasificacion"));
 
 
--Homologacion Relacion MetodoRelacionado:Directa
CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.NormativaRelacion" (
 "IdNormativaRelacion"		BIGINT					NOT NULL,
 "IdNormativa"              NVARCHAR(15)            NOT NULL,
 "IdTipoRelacion"			NVARCHAR(6)				DEFAULT '' 	NOT NULL,
 "CodigoRelacionNorma"      NVARCHAR(10)            DEFAULT ''  NOT NULL,
 "DescripcionRelacionNorma" NVARCHAR(100)           DEFAULT ''  NOT NULL,
 "IdEstado"                 NVARCHAR(3)             DEFAULT 'ACT' NOT NULL,
 PRIMARY KEY ("IdNormativaRelacion"));
 
 
 CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.ArchivoNormativa"(
	"IdArchivoNormativa"	NVARCHAR(40)	NOT NULL,
	"VersionArchivo"		NVARCHAR(10) 	NOT NULL,
	"Esquema"				NVARCHAR(10) 	NOT NULL,
	"Version" 				NVARCHAR(10) 	NOT NULL,
	"IdTipoDocumento" 		NVARCHAR(10) 	NOT NULL DEFAULT '',
	"Descripcion"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"NombreArchivo"			NVARCHAR(100)	NOT NULL DEFAULT '',
	"IdArchivo"				NVARCHAR(50)	NOT NULL DEFAULT '',
	"TipoArchivo"			NVARCHAR(6)		NOT NULL DEFAULT '',
	"ServicioArchivo"		NVARCHAR(300)	NOT NULL DEFAULT '',
	"IdEmpresa"				BIGINT			NOT NULL DEFAULT 0,
	"IdEstado"				NVARCHAR(3)		NOT NULL DEFAULT '',
	PRIMARY KEY ("IdArchivoNormativa","VersionArchivo","Esquema","Version")
);
 
 --HISTORICAS

CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.NCG380RegistroHistoricoCliente"(

"IdRegistroHistoricoCliente" BIGINT NOT NULL,
"IdCliente" BIGINT NOT NULL,
"DocumentoIdentidadRut" BIGINT,
"DocumentoIdentidadDig" NVARCHAR(1) DEFAULT ''	NULL,
"DocumentoIdentidadPasaporte" NVARCHAR(100) DEFAULT ''	NULL,
"Glei" NVARCHAR(500) DEFAULT ''	NULL,
"ApellidoPaterno" NVARCHAR(100) DEFAULT ''	NULL,
"ApellidoMaterno" NVARCHAR(100) DEFAULT ''	NULL,
"PrimerNombre" NVARCHAR(100) DEFAULT ''	NULL,
"SegundoNombre" NVARCHAR(100) DEFAULT ''	NULL,
"RazonSocial" NVARCHAR(100) DEFAULT ''	NULL,
"Telefono" NVARCHAR(40) DEFAULT ''	NULL,
"DireccionElectronica" NVARCHAR(100) DEFAULT ''	NULL,
"TipoDireccion" NVARCHAR(100) DEFAULT ''	NULL,
"Calle" NVARCHAR(100) DEFAULT ''	NULL,
"Numero" NVARCHAR(100) DEFAULT ''	NULL,
"Departamento" NVARCHAR(100) DEFAULT ''	NULL,
"Comuna" NVARCHAR(100) DEFAULT ''	NULL,
"Ciudad" NVARCHAR(100) DEFAULT ''	NULL,
"Provincia" NVARCHAR(100) DEFAULT ''	NULL,
"Pais" NVARCHAR(100) DEFAULT ''	NULL,
"CodigoPostal" NVARCHAR(100) DEFAULT ''	NULL,
"Nacionalidad" NVARCHAR(100) DEFAULT ''	NULL,
"RelacionEntidad" NVARCHAR(100) DEFAULT ''	NULL,
"ContratoAdminCartera" NVARCHAR(1) DEFAULT ''	NULL,
"EmpleadorRut" BIGINT,
"EmpleadorDig" NVARCHAR(10) DEFAULT ''	NULL,
"EmpleadorRazonSocial" NVARCHAR(100) DEFAULT ''	NULL,
"EstadoCivil" NVARCHAR(20) DEFAULT ''	NULL,
"ConyugeRut" BIGINT,
"ConyugeDig" NVARCHAR(1) DEFAULT ''	NULL,
"ConyugePasaporte" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeApellPaterno" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeApellMaterno" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugePrimerNombre" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeSegundoNombre" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeRazonSocial" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeTelefono" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeDireccionElectronica" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeTipoDireccion" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeCalle" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeNumero" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeDepartamento" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeComuna" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeCiudad" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeProvincia" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugePais" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeCodigoPostal" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeNacionalidad" NVARCHAR(100) DEFAULT ''	NULL,
"ConyugeRelacionEntidad" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalRut" BIGINT,
"RepLegalDig" NVARCHAR(10) DEFAULT ''	NULL,
"RepLegalPasaporte" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalGlei" NVARCHAR(500) DEFAULT ''	NULL,
"RepLegalApellPaterno" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalApellMaterno" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalPrimerNombre" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalSegundoNombre" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalRazonSocial" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalTelefono" NVARCHAR(40) DEFAULT ''	NULL,
"RepLegalDireccionElectronica" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalTipoDireccion" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalCalle" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalNumero" NVARCHAR(40) DEFAULT ''	NULL,
"RepLegalDepartamento" NVARCHAR(40) DEFAULT ''	NULL,
"RepLegalComuna" NVARCHAR(40) DEFAULT ''	NULL,
"RepLegalCiudad" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalProvincia" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalPais" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalCodigoPostal" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalNacionalidad" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalRelacionEntidad" NVARCHAR(100) DEFAULT ''	NULL,
"RepLegalEstadoCivil" NVARCHAR(20) DEFAULT ''	NULL,
"ConyRepLegalRut" BIGINT ,
"ConyRepLegalDig" NVARCHAR(10) DEFAULT ''	NULL,
"ConyRepLegalPasaporte" NVARCHAR(100) DEFAULT ''	NULL,		
"ConyRepLegalApellPaterno" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalApellMaterno" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalPrimerNombre" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalSegundoNombre" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalRazonSocial" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalTelefono" NVARCHAR(40) DEFAULT ''	NULL,
"ConyRepLegalDireccionElectronica" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalTipoDireccion" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalCalle" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalNumero" NVARCHAR(40) DEFAULT ''	NULL,
"ConyRepLegalDepartamento" NVARCHAR(40) DEFAULT ''	NULL,
"ConyRepLegalComuna" NVARCHAR(40) DEFAULT ''	NULL,
"ConyRepLegalCiudad" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalProvincia" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalPais" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalCodigoPostal" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalNacionalidad" NVARCHAR(100) DEFAULT ''	NULL,
"ConyRepLegalRelacionEntidad" NVARCHAR(100) DEFAULT ''	NULL,
"CuentaBancariaUno" NVARCHAR(100) DEFAULT ''	NULL,
"CuentaBancariaDos" NVARCHAR(100) DEFAULT ''	NULL,
"CuentaBancariaTres" NVARCHAR(100) DEFAULT ''	NULL,
"ClaseOrden" NVARCHAR(100) DEFAULT ''	NULL,
"PerfilActual" NVARCHAR(40) DEFAULT ''	NULL,
"PerfilAnterior" NVARCHAR(40) DEFAULT ''	NULL,
"FechaInicioPerfilActual"	DATE DEFAULT '0001-01-01'  NULL,
"EstadoRegistro" NVARCHAR(10),
"Concatenados" NVARCHAR(4000),
"FechaRegistro"	DATE DEFAULT '0001-01-01'  NULL,
PRIMARY KEY ("IdRegistroHistoricoCliente")		
);





CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.LogNormativa"(
"IdLogNormativa"		BIGINT          NOT NULL,
"IdTipoLogNormativa"	BIGINT          NOT NULL,
"IdUsuario"				BIGINT          NOT NULL,
"FechaLog"				NVARCHAR(40)	NOT NULL,
"IdProceso" 			BIGINT          NULL DEFAULT 0,
"IdSolicitudCircular" 	BIGINT          NULL DEFAULT 0,
"IdOperacion"			BIGINT			NULL DEFAULT 0,
"Registro"			    NVARCHAR(500)	NOT NULL DEFAULT '',
"Observacion"			NVARCHAR(500)	NOT NULL DEFAULT '',
PRIMARY KEY ("IdLogNormativa")
);


CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.TipoLogNormativa"(
"IdTipoLogNormativa"	BIGINT          NOT NULL,
"Nombre"				NVARCHAR(50)	NOT NULL,
PRIMARY KEY ("IdTipoLogNormativa")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.Indicador" (
 "IdTipoDocumento" 					NVARCHAR(10) 	NOT NULL DEFAULT '',
 "CodigoTipoIndicador"   			NVARCHAR(10) 	NOT NULL DEFAULT '',
 "IdUso"							NVARCHAR(3) 	DEFAULT 'IND' 	NOT NULL,
  PRIMARY KEY ("IdTipoDocumento","CodigoTipoIndicador","IdUso")
  );


CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.ClienteFatca"(
"IdClienteFatca"				BIGINT NOT NULL 
,"IdPersona"                    BIGINT NOT NULL    
,"IdCliente"                    BIGINT NOT NULL
,"TipoFatca"                    NVARCHAR(10) NULL  DEFAULT ''
,"ClasificacionFatca"           NVARCHAR(10) NULL  DEFAULT ''
,"ClasificacionEmpFatca"        NVARCHAR(10) NULL  DEFAULT ''
,"IndUsPerson"					NVARCHAR(10) NULL  DEFAULT ''
,"NumeroGiin"                   NVARCHAR(10) NULL  DEFAULT ''
,"FechaFormulario"              NVARCHAR(10) NULL  DEFAULT ''
,"AutFatca"                     NVARCHAR(10) NULL  DEFAULT ''
,"PaisNacimiento"               NVARCHAR(10) NULL  DEFAULT ''
,"TinPrimario"                  NVARCHAR(10) NULL  DEFAULT ''
,"PaisResidencia"               NVARCHAR(10) NULL  DEFAULT ''
,"NacionalidadSecundaria"       NVARCHAR(10) NULL  DEFAULT ''
,"TinSecundaria"                NVARCHAR(10) NULL  DEFAULT ''
,"NacionalidadTerciaria"        NVARCHAR(10) NULL  DEFAULT ''
,"TinTerciara"                  NVARCHAR(10) NULL  DEFAULT ''
,"NacionalidadCuaternaria"      NVARCHAR(10) NULL  DEFAULT ''
,"TinCuaternaria"               NVARCHAR(10) NULL  DEFAULT ''
,"NacionalidadQuinaria"         NVARCHAR(10) NULL  DEFAULT ''
,"TinQuinaria"                  NVARCHAR(10) NULL  DEFAULT ''
,"IndResidente"                 NVARCHAR(10) NULL  DEFAULT ''
,"IndNacimiento"                NVARCHAR(10) NULL  DEFAULT ''
,"IndDomicilio"                 NVARCHAR(10) NULL  DEFAULT ''
,"IndTelefono"                  NVARCHAR(10) NULL  DEFAULT ''
,"IndOrden"                     NVARCHAR(10) NULL  DEFAULT ''
,"IndFirma"                     NVARCHAR(10) NULL  DEFAULT ''
,"CliFechaModificacion"         NVARCHAR(10) NULL  DEFAULT ''
,PRIMARY KEY ("IdClienteFatca")
);

CREATE COLUMN TABLE "BSCL.Normativa.BD::NOR.DatosFatca"(
"IdDatosFatca"						BIGINT NOT NULL,
"IdPersona"                         BIGINT NOT NULL
"IdCliente"							BIGINT NOT NULL,
"rut_cli"							NVARCHAR(15) NULL,
"tipo_persona"                      NVARCHAR(20) NULL,
"ind_us_person"                     NVARCHAR(10) NULL  DEFAULT '0',
"clasificacion_fatca"               NVARCHAR(10) NULL,
"clasificacion_empfatca"            NVARCHAR(10) NULL,
"indicador_fatca"                   NVARCHAR(10) NULL  DEFAULT '0',
"auth_fatca"                        NVARCHAR(10) NULL,
"patrimonio_peso"                   NVARCHAR(50) NULL,--DECIMAL(24,3) DEFAULT 0,
"patrimonio_dolar"                  NVARCHAR(50) NULL,--DECIMAL(24,3) DEFAULT 0,
"fecha_enrolamiento"                NVARCHAR(10)  NULL,
"fecha_ult_modificacion"            NVARCHAR(10)  NULL,
"fecha_ult_modificacion_fatca"      NVARCHAR(10)  NULL,
"ind_residencia"                    NVARCHAR(1)   NULL  DEFAULT '0',
"ind_nacimiento"                    NVARCHAR(1)   NULL  DEFAULT '0',
"ind_pais_constitucion"             NVARCHAR(1)   NULL  DEFAULT '0',
"ind_casa_matriz"                   NVARCHAR(1)   NULL  DEFAULT '0',
"ind_nacionalidad"                  NVARCHAR(1)   NULL  DEFAULT '0',
"ind_direccion"                     NVARCHAR(1)   NULL  DEFAULT '0',
"ind_telefono"                      NVARCHAR(1)   NULL  DEFAULT '0',
"ind_ordenes"                       NVARCHAR(1)   NULL  DEFAULT '0',
"ind_firma"                         NVARCHAR(1)   NULL  DEFAULT '0',
"ind_benef_empresa"                 NVARCHAR(1)   NULL  DEFAULT '0',
--"fecha_carga_archivo"               NVARCHAR(10)  NULL,
"fecha_nuevos_indicios"             NVARCHAR(10)  NULL,
"fecha_firma_fatca"                 NVARCHAR(10)  NULL,
"fecha_vencimiento_fatca"           NVARCHAR(10)  NULL,
"concatenados"						NVARCHAR(500) NULL,
"Estado"							NVARCHAR(1)   NULL, -- V :vigente o C : caducado
"FechaCargaInformacion"     		NVARCHAR(10)  NULL,
PRIMARY KEY ("IdDatosFatca")
);
 
 
									    								     									     
 /*==============================================================*/
/* Sequences:       DROP & CREATE                               */
/*==============================================================*/

drop sequence "BSCL.Normativa.BD.Secuencias::SolicitudParametros";
create sequence "BSCL.Normativa.BD.Secuencias::SolicitudParametros" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;


drop sequence "BSCL.Normativa.BD.Secuencias::Documento";
create sequence "BSCL.Normativa.BD.Secuencias::Documento" increment by 1 start with 3 minvalue 1 maxvalue 4611686018427387903 no cycle;

drop sequence "BSCL.Normativa.BD.Secuencias::LogNormativo";
create sequence "BSCL.Normativa.BD.Secuencias::LogNormativo" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;

drop sequence "BSCL.Normativa.BD.Secuencias::DatosFatca";
create sequence "BSCL.Normativa.BD.Secuencias::DatosFatca" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;


drop sequence "BSCL.Normativa.BD.Secuencias::ClienteFatca";
create sequence "BSCL.Normativa.BD.Secuencias::ClienteFatca" increment by 1 start with 1 minvalue 1 maxvalue 4611686018427387903 no cycle;


/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit





