SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables: BSCL.Mesa.BD::SPT                              */
/*==============================================================*/
DROP TABLE "BSCL.APV.BD::SCS.Empleador";
DROP TABLE "BSCL.APV.BD::APV.Parametro";
DROP TABLE "BSCL.APV.BD::SCS.EntidadRecaudadora";
DROP TABLE "BSCL.APV.BD::SCS.TipoEntidadRecaudadora";
DROP TABLE "BSCL.APV.BD::SCS.MedioRecaudacion";
DROP TABLE "BSCL.APV.BD::SCS.ContratoSAAPV";
DROP TABLE "BSCL.APV.BD::SCS.DistribucionInversion";
DROP TABLE "BSCL.APV.BD::APV.Estado";
DROP TABLE "BSCL.APV.BD::APV.EstadoTransicion";
DROP TABLE "BSCL.APV.BD::APV.TipoCarga";
DROP TABLE "BSCL.APV.BD::APV.CargaMasiva";
DROP TABLE "BSCL.APV.BD::REC.Recaudacion";
DROP TABLE "BSCL.APV.BD::APV.PortafolioContratoSAAPV";
DROP TABLE "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV";
DROP TABLE "BSCL.APV.BD::REC.Empleador";
DROP TABLE "BSCL.APV.BD::REC.Cliente";
DROP TABLE "BSCL.APV.BD::REC.RegimenTributario";
DROP TABLE "BSCL.APV.BD::REC.Origen";
DROP TABLE "BSCL.APV.BD::REC.AsignacionContratoRecaudacion";
DROP TABLE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV";
DROP TABLE "BSCL.APV.BD::SCS.PlanTipoInversion";
DROP TABLE "BSCL.APV.BD::APV.ProductoAgrupador";
DROP TABLE "BSCL.APV.BD::TRA.DetalleTraspaso";
DROP TABLE "BSCL.APV.BD::TRA.Traspaso";
DROP TABLE "BSCL.APV.BD::APV.OrigenDestinoTransicion";
DROP TABLE "BSCL.APV.BD::REC.CodigoBancoPrevired";

/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/

CREATE COLUMN TABLE "BSCL.APV.BD::SCS.Empleador"
( "IdEmpleador"				BIGINT			not null
, "IdEmpresa"				BIGINT			not null
, "IdPersona" 				BIGINT			not null
, "EncargadoRRHH"			NVARCHAR(40)	not null
, "EMailRRHH"			    NVARCHAR(40)	NOT NULL
, "TelefonoRRHH"			NVARCHAR(40) 	NOT NULL
, "NumeroEmpleados"			BIGINT		 	NOT NULL
, "IdTipoSociedad"			NVARCHAR(40)	not null
, "FechaCreacion"           SECONDDATE		not null
, "FechaModificacion"		TIMESTAMP		not null
, "IdEstado"				NVARCHAR(3)		not null
,primary key ("IdEmpleador")
); 

CREATE COLUMN TABLE "BSCL.APV.BD::APV.Parametro"
( "IdEmpresa"			BIGINT			NOT NULL
, "IdParametro"			BIGINT 			NOT NULL
, "TipoParametro"		NVARCHAR(100)	NOT NULL
, "NombreParametro"		NVARCHAR(40)	NOT NULL	
, "Descripcion"			NVARCHAR(500)	NOT NULL	
, "ValorParametro"		NVARCHAR(100)	NOT NULL
, PRIMARY key("IdParametro")
);

CREATE COLUMN TABLE "BSCL.APV.BD::SCS.EntidadRecaudadora" (
	"IdEntidadRecaudadora"		BIGINT			NOT NULL,
	"IdPersona" 				BIGINT			NOT NULL,
	"Codigo"					BIGINT			NOT NULL,
	"Nombre"					NVARCHAR(20)	NOT NULL,
	"Estado"					NVARCHAR(10)	NOT NULL,
	"IdTipoEntidadRecaudadora"	BIGINT			NOT NULL,
PRIMARY KEY ("IdEntidadRecaudadora") 
);

CREATE COLUMN TABLE "BSCL.APV.BD::SCS.TipoEntidadRecaudadora" (
	"IdTipoEntidadRecaudadora"	BIGINT			NOT NULL,
	"Nombre" 					NVARCHAR(50)	NOT NULL,
	"Estado"					NVARCHAR(10)	NOT NULL,
PRIMARY KEY ("IdTipoEntidadRecaudadora") 
);

CREATE COLUMN TABLE "BSCL.APV.BD::SCS.MedioRecaudacion" (
	"IdMedioRecaudacion"		BIGINT			NOT NULL,
	"CodigoMedioRecaudacion"	NVARCHAR(50)	NOT NULL,
	"NombreMedioRecaudacion"	NVARCHAR(100)	NOT NULL,
	"TipoMedioRecaudacion"		NVARCHAR(10)	NOT NULL,
PRIMARY KEY ("IdMedioRecaudacion") 
);

CREATE COLUMN TABLE  "BSCL.APV.BD::SCS.ContratoSAAPV"(
	"IdContratoSAAPV"				BIGINT  		NOT NULL,
	"IdEmpresa"						BIGINT 			NOT NULL,
    "FolioContratoSAAPV" 			NVARCHAR(50)	NOT NULL,
    "IdPlan" 						BIGINT			NOT NULL,
	"NombrePlan" 					NVARCHAR(100)	NOT NULL,
	"FechaSuscripcion" 				DATE			NOT NULL,
	"FechaCreacion" 				DATE			,
	"FechaAprobacion" 				DATE			,
	"FechaNotificacion"				DATE			,
	"FechaRecepcion" 				DATE			,
	"Folio"							NVARCHAR(50)  	NOT NULL,
	"IdPersona" 					BIGINT 		 	,
	"IdEmpleador" 					BIGINT  		,
	"IdEnrolamiento"				BIGINT  		,
	"IdPortafolio"					BIGINT  		,
--
	"IdCliente" 					BIGINT  		,
	"NombreCliente"					NVARCHAR(100)	,
	"ApellidoPaternoCliente"		NVARCHAR(100)	,
	"ApellidoMaternoCliente"		NVARCHAR(100)	,
	"DocumentoIdentidadCliente"		NVARCHAR(40)	NOT NULL,
	"FechaNacimientoCliente"		DATE			,
	"EstadoCivil"					NVARCHAR(2)		,
	"IdTipoGenero"					NVARCHAR(2)		,	
	"IdPaisResidenciaCliente"		BIGINT			,
	"IdCiudadCliente"				BIGINT			,
	"IdComunaCliente"				BIGINT			,
	"DireccionCliente"				NVARCHAR(100)	,
	"IdTipoCargoPersona"			BIGINT			,
	"TelefonoCliente"				NVARCHAR(40)	,
	"TelefonoComercialCliente"		NVARCHAR(40)	,
	"TelefonoCelularCliente"		NVARCHAR(40)	,
	"EmailCliente"					NVARCHAR(100)	,
--	
	"RazonSocialEmpleador"			NVARCHAR(100)	,
	"DocumentoIdentidadEmpleador"	NVARCHAR(40)	,
	"DireccionComercialEmpleador"	NVARCHAR(100)	,
	"IdComunaEmpleador"				BIGINT			,
	"TelefonoEmpleador"				NVARCHAR(40)	,
	"NombreEncargado"				NVARCHAR(100)	,
	"EmailEncargado"				NVARCHAR(100)	,
	"TelefonoEncargado"				NVARCHAR(40)	,
--	

	"IdMedioRecaudacionTrabajador"	BIGINT			NOT NULL,
	"IdEntidadRecaudadora"			BIGINT			NOT NULL,
	"CodigoPrevision"				BIGINT			NOT NULL,
	"Pensionado"		        	NVARCHAR(2) 	NOT NULL,
	"Trabajador"					NVARCHAR(20)  	NOT NULL,
	"FormaDePago"					NVARCHAR(10)	,
	"IdMedioRecaudacionFormaPago"   BIGINT		 	,
    "DiaDescuento" 					BIGINT			,
    "Institucion" 					NVARCHAR(100)	,
	"ComisionTransferencia" 		DECIMAL(26,8)	,
	"NombreEntidadOrigen" 			NVARCHAR(100)	,
	"RutEntidadOrigen"				NVARCHAR(11)  	,
	"TipoDeAhorro" 					NVARCHAR(10)  	,
	"FondoDeOrigen" 				NVARCHAR(10)  	,
	"TipoTraspaso"					NVARCHAR(20) 	,
	"MontoTraspaso" 				DECIMAL(26,8)	,
	"UFTraspaso"					DECIMAL(26,8)	,
	"PorcentajeTraspaso"			DECIMAL(26,8)	,
	"IdEstadoEnvioNotificacion"		NVARCHAR(3)		,
	"IdEstadoSolicitud"				NVARCHAR(3)		,
	"IdEstado"						NVARCHAR(3) 	NOT NULL,
	"IdCargaMasiva"					BIGINT,			
	PRIMARY KEY ("IdContratoSAAPV")
);

CREATE COLUMN TABLE "BSCL.APV.BD::SCS.DistribucionInversion" (
     "IdDistribucionInversion"		BIGINT			NOT NULL
    ,"IdContratoSAAPV"				BIGINT  		NOT NULL
	,"FolioContratoSAAPV" 			NVARCHAR(50)	NOT NULL
	,"IdInstrumento"				BIGINT			NOT NULL
	,"Nemotecnico"					NVARCHAR(40)	NOT NULL
	,"MontoPesos" 					DECIMAL(26,8)	NOT NULL
	,"Porcentaje"	 				DECIMAL(26,8)	NOT NULL
	,"IdEstado"						NVARCHAR(3)		NOT NULL
);

CREATE COLUMN TABLE "BSCL.APV.BD::APV.Estado" (
 "IdEstado" 				BIGINT  	 	NOT NULL ,
 "Codigo"	 				NVARCHAR(40) 	NOT NULL ,
 "Nombre" 					NVARCHAR(100) 	NOT NULL , 
 "NombreEntidad" 			NVARCHAR(100) 	NOT NULL , 
 PRIMARY KEY ("IdEstado"));
 
 CREATE COLUMN TABLE "BSCL.APV.BD::APV.EstadoTransicion" (
 "IdEstadoTransicion" 		BIGINT  	 	NOT NULL ,
 "Tipo"						NVARCHAR(40)    NOT NULL ,
 "IdRegistro"				BIGINT			NOT NULL ,
 "FechaTransicion" 			SECONDDATE		NOT NULL , 
 "EstadoAnterior" 			NVARCHAR(3) 	NOT NULL , 
 "EstadoNuevo"  			NVARCHAR(3) 	NOT NULL , 
 "Observacion"  			NVARCHAR(520) 	NOT NULL , 
 "Usuario"  				NVARCHAR(100) 	NOT NULL ,
 PRIMARY KEY ("IdEstadoTransicion"));
 
 
CREATE TABLE "BSCL.APV.BD::APV.TipoCarga"(
	"IdEmpresa"				BIGINT	NOT NULL,
	"IdTipoCarga"			BIGINT	NOT NULL,
	"NombreTipoCarga"		NVARCHAR(100) NOT NULL,
	"CodigoTipoCarga"		NVARCHAR(3) NOT NULL,
	"IdEstado"				NVARCHAR(3) NOT NULL,
	PRIMARY KEY("IdTipoCarga")
);

CREATE TABLE "BSCL.APV.BD::APV.CargaMasiva"(
	"IdEmpresa"				BIGINT 			NOT NULL,
	"IdCargaMasiva"			BIGINT			NOT NULL,
	"NombreCargaArchivo"	NVARCHAR(100) 	NOT NULL,
	"CantidadRegistros"		BIGINT,
	"IdUsuario"				BIGINT,
	"IdTipoCarga"			BIGINT 			NOT NULL,
	"Descripcion"			NVARCHAR(200),
	"FechaCarga"			DATE,
	"IdEstado"				NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdCargaMasiva")	
); 
 
CREATE TABLE "BSCL.APV.BD::REC.Recaudacion"(
	"IdRecaudacion"			BIGINT 			NOT NULL,
	"IdEmpresa"				BIGINT			NOT NULL,
	"CodigoArchivo"			NVARCHAR(10),
	"FolioCartola"			BIGINT,
	"LineaCartola"			BIGINT,
	"Sucursal"				NVARCHAR(200)	NOT NULL,
	"FechaDeposito"			DATE 			NOT NULL,
	"CuentaCorriente"		BIGINT			NOT NULL,
	"MontoDeposito"			DECIMAL(26,8)	NOT NULL,
	"ConceptoRecaudacion"	NVARCHAR(300),
	"Origen"				NVARCHAR(100),
	"Observacion"			NVARCHAR(200),
	"NumeroDocumento"		BIGINT			NOT NULL,
	"IdTipoDeposito"		NVARCHAR(6)		NOT NULL,
	"IdBanco"				BIGINT			NOT NULL,
	"IdRegimenTributario"	BIGINT,
	"IdCarga"				BIGINT			NOT NULL,
	"IdEstado"				NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdRecaudacion")	
); 
 
CREATE TABLE "BSCL.APV.BD::SCS.PortafolioContratoSAAPV"(
	"IdPortafolioContratoSAAPV"	BIGINT 			NOT NULL,
	"IdPortafolio"				BIGINT			NOT NULL,
	"IdContratoSAAPV"			BIGINT			NOT NULL,
	"IdEstado"					NVARCHAR(3)		NOT NULL,
	"OrigenAhorro"				NVARCHAR(10)			NULL,
    "MontoPesos" 				DECIMAL(26,8)			NULL,
    "MontoUF" 					DECIMAL(26,8)			NULL,
    "PorcentajeRegimenImponible"	DECIMAL(26,8)			NULL,
    "Pago" 						NVARCHAR(20)			NULL,
    "PeriodoInicio" 			NVARCHAR(7)			    NULL,
    "PeriodoTermino" 			NVARCHAR(7)			    NULL);
	PRIMARY KEY("IdPortafolioContratoSAAPV")	
);

CREATE TABLE "BSCL.Portafolio.BD::POR.MatrizAgrupadorAPV"(
	"IdMatrizAgrupadorAPV"	BIGINT 			NOT NULL,
	"IdPropositoPortafolio" NVARCHAR(40)	NOT NULL,
	"IdPlanTipoInversion"	INT				NOT NULL,
	"IdProductoAgrupador"	INT				NOT NULL,
	"IdRegimenTributario"	BIGINT			NOT NULL,
	"NombreAgrupador"		NVARCHAR(100)	NOT NULL,
	"CodigoAgrupador"		NVARCHAR(100)	NOT NULL,
	"IdEstado"				NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdMatrizAgrupadorAPV","IdRegimenTributario","NombreAgrupador")	
);


CREATE TABLE "BSCL.APV.BD::SCS.PlanTipoInversion"(
	"IdPlanTipoInversion"		INT NOT NULL,
	"NombrePlanTipoInversion"	NVARCHAR(10),
	"IdEstado"					NVARCHAR(3),
	PRIMARY KEY("IdPlanTipoInversion")
);

CREATE TABLE "BSCL.APV.BD::APV.ProductoAgrupador"(
	"IdProductoAgrupador"		INT NOT NULL,
	"NombreProductoAgrupador"	NVARCHAR(40),
	"IdPlanTipoInversion"		INT,
	"IdEstado"					NVARCHAR(3),
	PRIMARY KEY("IdProductoAgrupador")
);

CREATE TABLE "BSCL.APV.BD::REC.Empleador"(
	"IdRecaudacionEmpleador"	BIGINT 			NOT NULL,
	"IdRecaudacion"				BIGINT,
	"CodigoArchivo"				NVARCHAR(10),
	"IdEmpresa"					BIGINT			NOT NULL,
	"IdPersona"					BIGINT			NOT NULL,
	"FechaIngreso"				DATE 			NOT NULL,
	"PeriodoPago"				NVARCHAR(7)		NOT NULL,
	"NumeroHojas"				BIGINT			NOT NULL,
	"TipoAfiliado"				NVARCHAR(3),
	"TipoPago"					NVARCHAR(50)	NOT NULL,
	"Insistencias"				NVARCHAR(50),
	"IdBanco"					BIGINT,
	"NumeroDocumento"			BIGINT,
	"MontoDepositoConvenido"	DECIMAL(26,8)	NOT NULL,
	"MontoDepositoAPV"			DECIMAL(26,8)	NOT NULL,
	"Reajustes"					DECIMAL(26,8)	NOT NULL,
	"Intereses"					DECIMAL(26,8)	NOT NULL,
	"Origen"					NVARCHAR(100)	NOT NULL,
	"Observaciones"				NVARCHAR(200),
	"IdEstado"					NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdRecaudacionEmpleador")	
); 
 
CREATE TABLE "BSCL.APV.BD::REC.Cliente"(
	"IdRecaudacionCliente"		BIGINT 			NOT NULL,
	"IdRecaudacion"				BIGINT,
	"IdRecaudacionEmpleador"	BIGINT,
	"IdEmpresa"					BIGINT			NOT NULL,
	"IdCliente"					BIGINT			NOT NULL,
	"FechaIngreso"				DATE 			NOT NULL,
	"TipoAfiliado"				NVARCHAR(3)		NOT NULL,
	"IdRegimenTriburario"		NVARCHAR(50)	NOT NULL,
	"MontoDepositoConvenido"	DECIMAL(26,8)	NOT NULL,
	"MontoDepositoAPV"			DECIMAL(26,8)	NOT NULL,
	"Origen"					NVARCHAR(100)	NOT NULL,
	"IdEstado"					NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdRecaudacionCliente")	
); 

CREATE TABLE "BSCL.APV.BD::REC.RegimenTributario"(
	"IdRegimenTributario"		BIGINT 			NOT NULL,
	"Codigo"					NVARCHAR(50)	NOT NULL,
	"IdEstado"					NVARCHAR(3)		NOT NULL,
	PRIMARY KEY("IdRegimenTributario")	
); 

CREATE COLUMN TABLE "BSCL.APV.BD::REC.AsignacionContratoRecaudacion" (
	"IdRecaudacionCliente"	  BIGINT			NOT NULL,
	"IdContratoSAAPV"	      BIGINT			NOT NULL,
 	"FechaCreacion"           SECONDDATE		NOT NULL,
 	"FechaModificacion"       SECONDDATE		NOT NULL,
PRIMARY KEY ("IdRecaudacionCliente") 
);

CREATE COLUMN TABLE "BSCL.APV.BD::REC.Origen"( 
	 "IdOrigen"		BIGINT			NOT NULL
	, "Codigo"		NVARCHAR(7)		NOT NULL
	, "Nombre"		NVARCHAR(100)	NOT NULL
	, "Tipo"		NVARCHAR(20)	NOT NULL
	, "IdEstado"	NVARCHAR(3)		NOT NULL
	,PRIMARY KEY ("IdOrigen")
); 

CREATE COLUMN TABLE "BSCL.APV.BD::TRA.Traspaso"(
	"IdTraspaso"				BIGINT	NOT NULL,
	"IdContratoSAAPV"			BIGINT,
	"FechaIngreso"				DATE,
	"FechaModificacion"			DATE,
	"CodigoTipoTraspaso"		NVARCHAR(10),
	"IdCliente"					BIGINT,
	"IdPersona"					BIGINT,
	"CodigoTipoAhorro"			NVARCHAR(10),	
	"NombreEntidadOrigen"		NVARCHAR(100),
	"DocumentoEntidadOrigen"	NVARCHAR(11),
	"FondoOrigen"				NVARCHAR(50),
	"MontoTotalTraspaso"		DECIMAL(26,8),
	"UFTotalTraspaso"			DECIMAL(26,8),
	"PorcentajeTotalTraspaso"	DECIMAL(26,8),
	"IdEstado"					NVARCHAR(3)
);

CREATE COLUMN TABLE "BSCL.APV.BD::TRA.DetalleTraspaso"(
	"IdTraspaso"				BIGINT	NOT NULL,
	"IdDetalleTraspaso"			BIGINT	NOT NULL,
	"IdPortafolio"				BIGINT,
	"FechaIngreso"				DATE,
	"FechaModificacion"			DATE,
	"IdPlanTipoInversion"		BIGINT,
	"IdProductoAgrupador"		BIGINT,
	"IdRegimenTributario"		BIGINT,
	"MontoPeso"					DECIMAL(26,8),
	"MontoUF"					DECIMAL(26,8),
	"Porcentaje"				DECIMAL(26,8),
	"IdEstado"					NVARCHAR(3)
);


CREATE COLUMN TABLE "BSCL.APV.BD::APV.OrigenDestinoTransicion"
( "IdEmpresa"					BIGINT		NOT NULL
, "IdOrigenDestinoTransicion"	BIGINT 		NOT NULL
, "Operacion"					NVARCHAR(3)	NOT NULL
, "MueveCuentaCorriente"		NVARCHAR(1)	NOT NULL	
, "MueveCuentaInversion"		NVARCHAR(1)	NOT NULL	
, "OrigenCtaBco"				BIGINT 		NOT NULL
, "SocioNegocioCtaBco"			BIGINT 		NOT NULL
, "DestinoCtaBco"				BIGINT 		NOT NULL
, "IdEstado"					NVARCHAR(3)	NOT NULL	
, PRIMARY key("IdOrigenDestinoTransicion")
);

CREATE COLUMN TABLE "BSCL.APV.BD::REC.CodigoBancoPrevired"
( "IdBanco"				BIGINT			NOT NULL
, "CodigoBancoPrevired"	NVARCHAR(200)	NOT NULL
);

