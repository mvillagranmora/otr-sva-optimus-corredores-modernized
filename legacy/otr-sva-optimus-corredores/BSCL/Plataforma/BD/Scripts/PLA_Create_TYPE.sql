SET SCHEMA "BSCL";

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/
DROP TABLE "BSCL.Plataforma.BD::PLA.DetalleLog"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio"
DROP TABLE "BSCL.Plataforma.BD::PLA.Boveda"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.CodigoError"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Configuracion"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Diccionario"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Filtro"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Log"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Modulo"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.OrigenPrecioTasa"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.ParametroFiltro"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Servicio"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Sucursal"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.TablaConversion"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoCanalIngreso"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoLog"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoReajuste"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.UsoMonedaFormato"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Concepto"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Moneda"  cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Empresa" cascade; 
DROP TABLE "BSCL.Plataforma.BD::PLA.Estado"  cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.EstadoPla" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.SolicitudCambio"  cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.Atributo" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoAtributo" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.DetalleAtributo" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.TipoRegistro" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.Archivo" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.Formato" cascade;
DROP TABLE "BSCL.Plataforma.BD::PLA.ArchivoFormato" cascade;
/*==============================================================*/
/* Create Table:                                                */
/*==============================================================*/


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Boveda" (
	 "IdBoveda" 	NVARCHAR(3) NOT NULL ,
	 "Codigo" 		NVARCHAR(40),
	 "Nombre"		NVARCHAR(100),
	 "IdEstado"		NVARCHAR(3), 
	 PRIMARY KEY ("IdBoveda")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CodigoError" (
	 "IdError" 		BIGINT CS_FIXED NOT NULL ,
	 "Codigo" 		NVARCHAR(40)    NOT NULL , 
	 PRIMARY KEY ("IdError")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Concepto" (
	 "IdConcepto" 	NVARCHAR(6) NOT NULL ,
	 "Tipo" 		NVARCHAR(2),
	 "IdModulo" 	NVARCHAR(3),
	 "Nombre" 		NVARCHAR(100),
	 "IdEstado" 	NVARCHAR(3), 
	 PRIMARY KEY ("IdConcepto")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" (
	 "IdCondicionDeLiquidacion" NVARCHAR(2) NOT NULL ,
	 "Codigo" 					NVARCHAR(40),
	 "Nombre" 					NVARCHAR(100),
	 "PlazoEnDias" 				INTEGER CS_INT, 
	 PRIMARY KEY ("IdCondicionDeLiquidacion")
);



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Configuracion" (
	 "IdEmpresa" 		BIGINT CS_FIXED NOT NULL ,
	 "CodigoParametro" 	NVARCHAR(40) 	NOT NULL ,
	 "Descripcion" 		NVARCHAR(520) 	NOT NULL ,
	 "Valor" 			NVARCHAR(520) 	NOT NULL , 
	 PRIMARY KEY ("IdEmpresa", "CodigoParametro")
);



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Diccionario" (
	 "CodDiccionario" 	NVARCHAR(100) 	NOT NULL ,
	 "CodLenguaje" 		NVARCHAR(40) 	NOT NULL ,
	 "Descripcion" 		NVARCHAR(520) 	NOT NULL ,
	 "Abreviado" 		NVARCHAR(520) 	DEFAULT '', 
	 PRIMARY KEY ("CodDiccionario", "CodLenguaje")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Empresa" (
	 "IdEmpresa" 		BIGINT CS_FIXED NOT NULL ,
	 "Codigo" 			NVARCHAR(40),
	 "Nombre" 			NVARCHAR(100),
	 "IdEstado" 		NVARCHAR(3),
	 "IdPais" 			BIGINT CS_FIXED,
	 "IdSocioDeNegocio" BIGINT CS_FIXED, 
	 PRIMARY KEY ("IdEmpresa")
);



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Estado" (
	 "IdEstado" 	NVARCHAR(3) NOT NULL ,
	 "Nombre" 		NVARCHAR(100), 
	 PRIMARY KEY ("IdEstado")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.EstadoPla" (
     "IdEstadoPla"      BIGINT NOT NULL,
     "IdEstado" 		NVARCHAR(3) NOT NULL,
     "NombreEntidad" 	NVARCHAR(100) NOT NULL,
     "Descripcion" 		NVARCHAR(520) NOT NULL, 
     PRIMARY KEY ("IdEstadoPla")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Filtro" (
	 "IdFiltro" 	BIGINT CS_FIXED NOT NULL ,
	 "IdRol" 		BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
	 "valor" 		NVARCHAR(40) 	DEFAULT '' NOT NULL ,
	 "spConsulta" 	NVARCHAR(1040) 	DEFAULT '' NOT NULL ,
	 "operador" 	NVARCHAR(40) 	DEFAULT 'eq' NOT NULL ,
	 "descripcion" 	NVARCHAR(1040) 	DEFAULT '' NOT NULL ,
	 "estado" 		NVARCHAR(1) 	DEFAULT 'S' NOT NULL , 
	 PRIMARY KEY ("IdFiltro")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Log" (
	 "IdLog" 					BIGINT CS_FIXED NOT NULL ,
	 "IdTipoLog" 				NVARCHAR(40),
	 "IdModulo"					NVARCHAR(3),
	 "IdUsuario"				BIGINT CS_FIXED,
	 "IdEmpresa"				BIGINT CS_FIXED,
	 "FechaLog"					LONGDATE CS_LONGDATE,
	 "PeriodoLog"				NVARCHAR(7),
	 "IdUsuarioAtendido"		BIGINT CS_FIXED,
	 "IdCliente"				BIGINT CS_FIXED,
	 "EstadoCliente"			NVARCHAR(3),
	 "NombreCliente"			NVARCHAR(100), 
	 "IdTipoDocumentoIdentidad"	NVARCHAR(40), 
	 "IdDocumentoIdentidad"		NVARCHAR(40), 
	 "IdPortafolio"				BIGINT CS_FIXED,
	 "EstadoPortafolio"			NVARCHAR(3),
	 "IdRegistro"				NVARCHAR(40),
	 "EstadoRegistro"			NVARCHAR(3),
	 "IdTipoRegistro"			NVARCHAR(40), 
	 "IdTipoOperacion"			NVARCHAR(40), 
	 "Folio"					BIGINT CS_FIXED,
	 "Cantidad"					INTEGER CS_INT,
	 "PrecioTasa"				INTEGER CS_INT,
	 "Monto"					INTEGER CS_INT,
	 "IdMonedaOrigen"			NVARCHAR(3),
	 "IdMonedaLiquidacion"		NVARCHAR(3),
	 "TipoCambio"				NVARCHAR(40),
	 "Nemo"						NVARCHAR(260),
	 "IdProceso"				BIGINT CS_FIXED,
	 "EstadoProceso"			NVARCHAR(3),
	 "NombreProceso"			NVARCHAR(100),
	 PRIMARY KEY ("IdLog")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Modulo" (
	 "IdModulo" 	NVARCHAR(3) NOT NULL ,
	 "Codigo" 		NVARCHAR(40),
	 "Nombre" 		NVARCHAR(100),
	 "IdEstado" 	NVARCHAR(3), 
	PRIMARY KEY ("IdModulo")
);



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Moneda" (
	 "IdMoneda" 					NVARCHAR(3) NOT NULL ,
	 "Codigo" 						NVARCHAR(40),
	 "CodigoNumerico" 				INTEGER CS_INT,
	 "Nombre" 						NVARCHAR(100),
	 "Simbolo" 						NVARCHAR(40),
	 "Decimales" 					INTEGER CS_INT,
	 "MultiplicaODivide" 			NVARCHAR(40),
	 "RangoSuperiorParidad" 		DECIMAL(26,8) CS_FIXED,
	 "RangoInferiorParidad" 		DECIMAL(26,8) CS_FIXED,
	 "PorcentajeSuperiorParidad" 	DECIMAL(26,8) CS_FIXED,
	 "PorcentajeInferiorParidad" 	DECIMAL(26,8) CS_FIXED,
	 "RangoSuperiorTipoCambio" 		DECIMAL(26,8) CS_FIXED,
	 "RangoInferiorTipoCambio" 		DECIMAL(26,8) CS_FIXED,
	 "PorcentajeSuperiorTipoCambio" DECIMAL(26,8) CS_FIXED,
	 "PorcentajeInferiorTipoCambio" DECIMAL(26,8) CS_FIXED,
	 "Orden" 						INTEGER CS_INT,
	 "IdPais" 						BIGINT CS_FIXED,
	 "IdEstado" 					NVARCHAR(3), 
	 "CodigoBancoCentral"			NVARCHAR(40) NOT NULL DEFAULT '900',
	 "DescripcionBancoCentral" 		NVARCHAR(100) NOT NULL DEFAULT 'OTRAS NO ESPECIFICADAS'
	PRIMARY KEY ("IdMoneda")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" (
	 "IdParidadYTipoCambio" 	BIGINT,
	 "Fecha" 					DATE        default '0001-01-01' not null,  
	 "IdMoneda"                 NVARCHAR(3) default '' not null,                
	 "IdUsuarioIngreso"         BIGINT default 0 not null,
	 "TipoCambioMercado" 		DECIMAL(26,8) default 0 not null,
	 "ParidadMercado"           DECIMAL(26,8) default 0 not null, 
	 "IdEstado"					NVARCHAR(3), 
	 "Origen" 					NVARCHAR(10) default 'OBSERVADO' -- Fuente de información del tipo de cambio de la moneda Pivote del sistema EJ: CORREDOR, OBSERVADO, BCENTRAL
	 PRIMARY KEY ("IdParidadYTipoCambio")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.OrigenPrecioTasa" (
	 "IdOrigen" 	BIGINT CS_FIXED NOT NULL ,
	 "Codigo" 		NVARCHAR(40),
	 "Nombre" 		NVARCHAR(100),
	 "IdEstado" 	NVARCHAR(3), 
	 PRIMARY KEY ("IdOrigen")
  );



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ParametroFiltro" (
	 "IdParametroFitro" BIGINT CS_FIXED NOT NULL ,
	 "IdFiltro" 		BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
	 "IdServicio" 		BIGINT CS_FIXED DEFAULT 0 NOT NULL ,
	 "estado" 			NVARCHAR(1) 	DEFAULT 'S' NOT NULL , 
	 PRIMARY KEY ("IdParametroFitro")
  );


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Servicio" (
	 "IdServicio" 		BIGINT CS_FIXED NOT NULL ,
	 "RutaServicio" 	NVARCHAR(520),
	 "UsoServicio" 		INTEGER CS_INT,
	 "TipoServicio" 	NVARCHAR(40) 	DEFAULT 'P' NOT NULL ,
	 "Descripcion" 		NVARCHAR(1040) 	DEFAULT '' NOT NULL , 
	 PRIMARY KEY ("IdServicio")
  );




CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Sucursal" (
	 "IdSucursal" 		BIGINT CS_FIXED NOT NULL ,
	 "IdEmpresa" 		BIGINT CS_FIXED,
	 "CodigoSucursal" 	NVARCHAR(40),
	 "NombreSucursal" 	NVARCHAR(520),
	 "EstadoSucursal" 	NVARCHAR(3), 
	 PRIMARY KEY ("IdSucursal")
  );



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TablaConversion" (
	 "IdTablaConversion" 	BIGINT CS_FIXED NOT NULL ,
	 "IdModulo" 			NVARCHAR(3),
	 "Codigo" 				NVARCHAR(40),
	 "Tipo" 				NVARCHAR(40),
	 "Valor" 				NVARCHAR(260),
	 "IdEstado" 			NVARCHAR(3), 
	 PRIMARY KEY ("IdTablaConversion")
 );


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoCanalIngreso" (
	 "IdTipoCanalIngreso" 	NVARCHAR(3) NOT NULL ,
	 "Codigo" 				NVARCHAR(40),
	 "Nombre" 				NVARCHAR(100),
	 "IdEstado" 			NVARCHAR(3), 
	 PRIMARY KEY ("IdTipoCanalIngreso")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoLog" (
	 "IdTipoLog" 	NVARCHAR(40) NOT NULL ,
	 "Descripcion" 	NVARCHAR(100), 
	 PRIMARY KEY ("IdTipoLog")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoOperacionYCondicionDeLiquidacion" (
	 "IdTipoOperacionYCondicionDeLiquidacion" 	BIGINT CS_FIXED NOT NULL ,
	 "SoloDiasHabiles" 							NVARCHAR(1),
	 "IdCondicionDeLiquidacion" 				NVARCHAR(2),
	 "IdTipoOperacion" 							NVARCHAR(6),
	 "IdEstado" 								NVARCHAR(3), 
	 PRIMARY KEY ("IdTipoOperacionYCondicionDeLiquidacion")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoReajuste" (
	 "IdTipoReajuste" 	NVARCHAR(3) 	NOT NULL ,
	 "Codigo" 			NVARCHAR(40),
	 "Nombre" 			NVARCHAR(100),
	 "Reajustable" 		NVARCHAR(1) 	DEFAULT 'N',
	 "IdEstado" 		NVARCHAR(3), 
	 PRIMARY KEY ("IdTipoReajuste")
);


CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" (
	 "IdMonedaFormato" 				BIGINT CS_FIXED 	NOT NULL ,
	 "IdMoneda" 					NVARCHAR(3) 		NOT NULL ,
	 "IdUsoMonedaFormato" 			NVARCHAR(3) 		DEFAULT '' NOT NULL ,
	 "IdTipoInstrumento" 			NVARCHAR(3) 		DEFAULT '' NOT NULL ,
	 "IdTipoOperacion" 				NVARCHAR(6) 		DEFAULT '' NOT NULL ,
	 "PrecioDecimales" 				INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "PrecioTrucaRedondea" 			NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "TasaDecimales" 				INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "TasaTrucaRedondea" 			NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "CantidadDecimales" 			INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "CantidadTrucaRedondea" 		NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "MontoDecimales" 				INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "MontoTrucaRedondea" 			NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "TipoCambioDecimales" 			INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "TipoCambioTrucaRedondea" 		NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "ParidadDecimales" 			INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "ParidadTrucaRedondea" 		NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "PuntosDecimales" 				INTEGER CS_INT 		DEFAULT 0 NOT NULL ,
	 "PuntosTrucaRedondea" 			NVARCHAR(1) 		DEFAULT 'T' NOT NULL ,
	 "IdEstado" 					NVARCHAR(3)      	NOT NULL , 
	 PRIMARY KEY ("IdMonedaFormato")
);
	 



CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" (
	 "IdUsoMonedaFormato" 	NVARCHAR(3) 		NOT NULL ,
	 "IdEmpresa" 			BIGINT CS_FIXED 	NOT NULL ,
	 "Codigo" 				NVARCHAR(40) 		NOT NULL ,
	 "Nombre" 				NVARCHAR(100) 		NOT NULL ,
	 "IdEstado" 			NVARCHAR(3) 		NOT NULL , 
	 PRIMARY KEY ("IdUsoMonedaFormato")
 );

create column table "BSCL.Plataforma.BD::PLA.SolicitudCambio" (
   "IdSolicitudCambio"  BIGINT               not null,
   "IdSecuenciaCambio"  BIGINT               not null,
   "IdTipoEntidad"      NVARCHAR(40)         not null,
   --"IdEntidadOrigen"    BIGINT               not null,
   "IdEntidadOrigen"    NVARCHAR(40)   		 default ''      not null,
   "CampoModificado"    NVARCHAR(255)        not null,
   "TipoDato"           NVARCHAR(255)        not null,
   "ValorAntiguo"       NVARCHAR(255)        not null,
   "ValorNuevo"         NVARCHAR(255)        not null,
   "FechaSolicitud"     SECONDDATE			 not null,
   "FechaRevision"      SECONDDATE			 not null,
   "IdEstadoAutorizacion" NVARCHAR(3)        not null,
   primary key ("IdSolicitudCambio","IdSecuenciaCambio")
);

create column table "BSCL.Plataforma.BD::PLA.Atributo" (
   "IdAtributo"         BIGINT		         not null,
   "IdEntidad"          BIGINT               not null,
   "IdTipoEntidad"      NVARCHAR(40)         not null,
   "IdTipoAtributo"  	BIGINT  			 DEFAULT 0  NOT NULL,
   "IdEmpresa"			BIGINT				 not null,
   "ValorAtributo"      NVARCHAR(255)        not null,
   "FechaIngreso"       SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaCreacion"      SECONDDATE           default '0001-01-01 00:00:00' not null,
   "FechaModificacion"  SECONDDATE           default '0001-01-01 00:00:00' not null,
   "IdEstadoAutorizado" NVARCHAR(3)          not null,
   primary key  ("IdEntidad", "IdTipoEntidad", "IdAtributo", "IdEmpresa")
);

create column table "BSCL.Plataforma.BD::PLA.TipoAtributo" (
   "IdTipoAtributo"         BIGINT               not null,
   "IdTipoEntidad"      	NVARCHAR(40)         not null,
   "IdEmpresa"				BIGINT				 not null,
   "ClaseDato"         		NVARCHAR(40)         not null,
   "NombreTipoAtributo"     NVARCHAR(255)        not null,
   "GrupoAtributo"       	NVARCHAR(40)           		 ,
   --"Comportamiento"      	NVARCHAR(40)                 ,
   "IdEstado" 				NVARCHAR(3) 	DEFAULT 'ACT' NOT NULL,
   "RequiereAutorizacion" 	NVARCHAR(1)		DEFAULT 'N' NOT NULL,
   "EsAtributo"				NVARCHAR(1) 	DEFAULT 'S' NOT NULL,
   primary key  ("IdTipoAtributo", "IdTipoEntidad", "IdEmpresa")
);
create column table "BSCL.Plataforma.BD::PLA.DetalleAtributo" (
   "IdDetalleAtributo"         BIGINT               not null,
   "IdTipoAtributo"      	   BIGINT		        not null,
   "DetalleAtributo"           NVARCHAR(255)         not null,
   primary key  ("IdDetalleAtributo", "IdTipoAtributo")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.EstadoTipoAtributo" (
 "IdEstadoTipoAtributo" BIGINT CS_FIXED NOT NULL ,
 "IdEstado" NVARCHAR(3) DEFAULT '' NOT NULL ,
 "NombreEntidad" NVARCHAR(100) DEFAULT '' NOT NULL , 
 PRIMARY KEY ("IdEstadoTipoAtributo"))  ;
 
 CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoDatoTipoAtributo" (
 "IdTipoDatoTipoAtributo" BIGINT CS_FIXED NOT NULL ,
 "TipoDato" NVARCHAR(20) NOT NULL,
 "NombreTipoDato" NVARCHAR(20) NOT NULL,
 PRIMARY KEY ("IdTipoDatoTipoAtributo")) ;

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.DetalleLog" (
	 "IdDetalleLog"			BIGINT CS_FIXED NOT NULL, 
	 "IdLog"				BIGINT CS_FIXED NOT NULL, 
	 "SubRegistro"			NVARCHAR(40), 
	 "CampoRegistro"		NVARCHAR(40), 
	 "ValorAntiguo"			NVARCHAR(250), 
	 "ValorNuevo"			NVARCHAR(250), 
	 PRIMARY KEY ("IdDetalleLog")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.TipoRegistro" (
	 "IdTipoRegistro"	NVARCHAR(40) NOT NULL ,
	 "Descripcion" 		NVARCHAR(100), 
	PRIMARY KEY ("IdTipoRegistro")
);

-- Maestro de configuracion de usos de portafolio. Portafolio -< UsoPortafolio >- ConfiguracionUsoPortafolio
create column table "BSCL.Plataforma.BD::PLA.ConfiguracionUsoPortafolio" (
   "CodigoUso"						NVARCHAR(40)	NOT NULL, 
   "IdEmpresa"						BIGINT			NOT NULL,
   "IdTipoRegistro"					NVARCHAR(40) 	NOT NULL DEFAULT '',
   "NombreGrupo"					NVARCHAR(100) 	NOT NULL DEFAULT '',
   "DescripcionUso"					NVARCHAR(40)	NOT NULL DEFAULT '', 
   "IdEstado"						NVARCHAR(3)		NOT NULL DEFAULT 'ACT', 
   "EsObligatorio"					NVARCHAR(1)		NOT NULL DEFAULT 'N',
   "IdTipoDocumento"				NVARCHAR(40)	NOT NULL DEFAULT '', 
   primary key  ("CodigoUso","IdEmpresa")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ConfiguracionFiltroInterfaz" (
	 "IdConfiguracionFiltroInterfaz" BIGINT CS_FIXED NOT NULL,
	 "Controlador" 	NVARCHAR(100)  DEFAULT '' NOT NULL , --CrearPrestamoModalCtrl
	 "TipoObjeto"	NVARCHAR(50)   DEFAULT '' NOT NULL , --combobox, grilla
	 "NombreObjeto"	NVARCHAR(100)  DEFAULT '' NOT NULL , --tipo-instrumento,combobox-bolsa
	 "IdEstado"		NVARCHAR(3)    DEFAULT '' NOT NULL ,
	 "Filtros"		NVARCHAR(2000) DEFAULT '' NOT NULL ,
	 PRIMARY KEY ("IdConfiguracionFiltroInterfaz")
);

---------Carga general de archivos
CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Archivo" (
   "IdArchivo" 				BIGINT              not null,
   "IdEmpresa"				BIGINT				not null,
   "CodigoArchivo"     		NVARCHAR(50)        not null unique,
   "TieneSeparador"			NVARCHAR(1)			not null,--S/N, en caso de ser 'N', de tomara el campo tamaño de la tabla formato para separar los campos.
   "SeparadorColumnas"		NVARCHAR(5)			not null,--Ej: . o , o ; u otro.			
   PRIMARY KEY("IdArchivo")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.Formato" (
   "IdFormato"				BIGINT              not null,
   "CodigoCampo"			NVARCHAR(100)		not null,--EJ: FECHA_TRANSACCION (en el caso de tener separador, es el nombre del campo en la cabecera)
   "TipoDato"  	    		NVARCHAR(50)        not null,--number, string
   "Posicion"				INT					not null,--En el caso de no tener separador, se debe especificar la posicion del campo en el archivo; caso contrario dejar en 0
   "LargoCampo"				INT					not null,--Largo en caracteres del campo a formatear (solo en caso de no tener separador el archivo)
   "EsObligatorio"			NVARCHAR(1)			not null, --S/N
   "Validacion"	     		NVARCHAR(50)        not null,--REGEX OPCIONAL, EJ: (.*)
   "MensajeValidacion"		NVARCHAR(150)		not null,--EJ:La fecha debe estar en formato YYYY-MM.	 
   PRIMARY KEY("IdFormato")
);

CREATE COLUMN TABLE "BSCL.Plataforma.BD::PLA.ArchivoFormato" (
   "IdArchivoFormato"		BIGINT              not null,
   "IdArchivo"				BIGINT				not null,
   "IdFormato"     	    	BIGINT      		not null,
   PRIMARY KEY("IdArchivoFormato")
);
/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/

/* Table: */

ALTER TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" WITH PARAMETERS ('CONCAT_ATTRIBUTE'=('$uc_INX01_MonedaFormato$','IdMoneda','IdUsoMonedaFormato','IdTipoInstrumento','IdTipoOperacion'));

CREATE UNIQUE INDEX "INX01_MonedaFormato" ON "BSCL.Plataforma.BD::PLA.MonedaFormato" (
	 "IdMoneda" ASC,
	 "IdUsoMonedaFormato" ASC,
	 "IdTipoInstrumento" ASC,
	 "IdTipoOperacion" ASC 
 );
 
alter table "BSCL.Plataforma.BD::PLA.TipoReajuste" 
       alter ("IdTipoReajuste" NVARCHAR(3) not null default '');

alter table "BSCL.Plataforma.BD::PLA.MonedaFormato"
add constraint "FK_Moneda_MonedaFormato" foreign key ("IdMoneda")
references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;
      
 
ALTER TABLE "BSCL.Plataforma.BD::PLA.MonedaFormato" 
ADD CONSTRAINT "FK_UsoMonedaFormato_MonedaFormato" FOREIGN KEY ( "IdUsoMonedaFormato" ) REFERENCES "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" (
 "IdUsoMonedaFormato") ON UPDATE RESTRICT ON DELETE RESTRICT;
 
ALTER TABLE "BSCL.Plataforma.BD::PLA.UsoMonedaFormato" ADD CONSTRAINT "FK_Empresa_UsoMonedaFormato" FOREIGN KEY ( "IdEmpresa" ) 
REFERENCES "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE "BSCL.Plataforma.BD::PLA.ParidadYTipoCambio" ADD CONSTRAINT "FK_IdMoneda_ParidadYTipoCambio" FOREIGN KEY ( "IdMoneda" ) 
REFERENCES "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") ON UPDATE RESTRICT ON DELETE RESTRICT;

ALTER TABLE "BSCL.Plataforma.BD::PLA.DetalleLog" 
ADD CONSTRAINT "FK_Log_DetalleLog" FOREIGN KEY ( "IdLog" ) 
REFERENCES "BSCL.Plataforma.BD::PLA.Log" ("IdLog") ON UPDATE RESTRICT ON DELETE RESTRICT;
/*==============================================================*/
/* Drop INDEX:                                                 */
/*==============================================================*/
DROP INDEX "INX01_MonedaFormato";


/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

/* Table: */
create unique index "INX01_MonedaFormato" on "BSCL.Plataforma.BD::PLA.MonedaFormato"(
											 "IdMoneda", "IdUsoMonedaFormato", "IdTipoInstrumento", "IdTipoOperacion");

/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/
commit



/*==============================================================*/
/* Index:                                                       */
/*==============================================================*/

