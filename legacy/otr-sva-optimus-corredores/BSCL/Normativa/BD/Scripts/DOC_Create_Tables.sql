set schema "BSCL";

-- Diccionario
-- Esquema
--     Campo
--     Clasificador
--         CampoClasificador
--     Cuadratura
--         CampoCuadratura
--     Homologacion
--     Parametro
--     Validacion
--     Documento ----------> TipoDocumento
--         ResumenCarga
--         ResumenGeneracion
--         ResumenCuadratura
--             DetalleCuadratura
--         ResumenValidacion
--             DetalleValidacion
--         DatosDJ1885
--         DatosDJ1891
--         ...

/*==============================================================*/
/* Drop Tables:                                                 */
/*==============================================================*/

drop table "BSCL.Normativa.BD::TRI.DetalleCuadratura";
drop table "BSCL.Normativa.BD::TRI.DetalleValidacion";
drop table "BSCL.Normativa.BD::TRI.ResumenCarga";
drop table "BSCL.Normativa.BD::TRI.ResumenCuadratura";
drop table "BSCL.Normativa.BD::TRI.ResumenGeneracion";
drop table "BSCL.Normativa.BD::TRI.ResumenValidacion";
drop table "BSCL.Normativa.BD::DOC.Documento";
drop table "BSCL.Normativa.BD::DOC.TipoDocumento";
drop table "BSCL.Normativa.BD::DOC.Validacion";
drop table "BSCL.Normativa.BD::DOC.Homologacion";
drop table "BSCL.Normativa.BD::DOC.Parametro";
drop table "BSCL.Normativa.BD::DOC.Diccionario";
drop table "BSCL.Normativa.BD::DOC.CampoCuadratura";
drop table "BSCL.Normativa.BD::DOC.Cuadratura";
drop table "BSCL.Normativa.BD::DOC.CampoClasificador";
drop table "BSCL.Normativa.BD::DOC.Clasificador";
drop table "BSCL.Normativa.BD::DOC.Campo";
drop table "BSCL.Normativa.BD::DOC.Esquema";
DROP TABLE "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor";
DROP TABLE "BSCL.Normativa.BD::DOC.TipoDocumentoUI";

/*==============================================================*/
/* Create Tables:                                               */
/*==============================================================*/

-- Table: "BSCL.Normativa.BD::DOC.Esquema"
-- Tabla que contiene datos de especificacion base de todos los
-- tipos de documentos.
-- Representa la raiz en la jerarquia.
create column table "BSCL.Normativa.BD::DOC.Esquema" (
    -- Nombre del esquema (Ej. DJ1891).
    "Esquema" nvarchar(10) not null,

    -- Version del esquema (Ej. 2015).
    "Version" nvarchar(10) not null,

    -- Modulo.
    "Modulo" nvarchar(10) not null,

    -- Periodicidad (Ej. "A"nual , "M"ensual, "T"rimestral, "D"iaria)
    "Periodicidad" char(1) not null,

    -- Texto descriptivo del objetivo de la declaracion.
    "Objetivo" nvarchar(100) not null,

    -- Fecha desde la cual se exige presentacion
    "PeriodoInicio" date not null,

    -- Identificador de la entidad reguladora (Ej. SII)
    "IdEntidadReguladora" nvarchar(40) not null,

    -- Fecha limite para presentacion en la entidad reguladora.
    "FechaPresentacionLimite" date not null,

    -- Fecha desde la cual se presentan los datos (inclusive).
    "FechaDatosDesde" date not null,

    -- Fecha hasta la cual se presentan los datos (inclusive).
    "FechaDatosHasta" date  not null,

    -- Codigo de version SII.
    "CodigoVersionSII" smallint not null,
    
    -- Indicador para mostrar en el mantenedor de DJ's
    "HabilitadoMantenedor" char(1) default 'N'

    primary key ("Esquema", "Version")
);
-- ALTER TABLE "BSCL.Normativa.BD::DOC.Esquema" ALTER ("FechaDatosHasta" date not null);
 
-- Table: "BSCL.Normativa.BD::DOC.Campo"
-- Especificacion de los campos del detalle de un documento,
-- en funcion de su representacion y formatos para el tratamiento
-- generico de los mismos en los programas correspondientes.
create column table "BSCL.Normativa.BD::DOC.Campo" (

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del campo (debe ser unico para esquema/version).
    "Nombre" nvarchar(40) not null,

    -- Codigo SII del campo.
    "CodigoSII" nvarchar(40) not null default '',

    -- Tipo de diccionario relacionado.
    "TipoDiccionario" nvarchar(40) not null default '',

    -- Orden de presentacion del campo.
    "Orden" smallint not null,

    -- Texto descriptivo del campo para ser utilizado como titulo.
    -- Debe ser unico para esquema/version.
    "Titulo" nvarchar(100) not null,

    -- Texto de ayuda del campo.
    "Ayuda" nvarchar(200) not null default '',

    -- Tipo de dato, segun los manejados por la aplicacion.
    "TipoDato" nvarchar(100) not null,

    -- Largo del campo.
    "Largo" smallint not null,

    -- Valor por defecto del campo.
    "ValorDefecto" nvarchar(100) not null,

    -- Indicador de si el campo es obligatorio (S/N).
    "Obligatorio" char(1) not null,

    -- Indicador de si el campo es editable (S/N).
    "Editable" char(1) not null,

    -- Indicador de si el campo es editable en modo masivo (S/N).
    "ModoMasivo" char(1) not null,

    -- Indicador de si el campo es exportable (S/N).
    "Exportable" char(1) not null,

    -- Indicador de si el campo es oculto en la grilla (S/N).
    "OcultoEnGrilla" char(1) not null,

    -- Indicador de si el campo es oculto en el mantenedor (S/N).
    "OcultoEnMantenedor" char(1) not null,

    -- Indicador de si la columna es comun (S/N).
    "Comun" char(1) not null,

    -- Indicador de si la columna es clave (S/N).
    "Clave" char(1) not null,

    -- Grupo de la columna.
    "Grupo" nvarchar(100) not null,

    -- Posición de disposicion del campo en archivo de carga.
    "OrdenCargaExterna" smallint not null,

    -- Cantidad de decimales.
    "CantidadDecimales" smallint not null default 0,

    primary key ("Esquema", "Version", "Nombre")
);

alter table "BSCL.Normativa.BD::DOC.Campo"
   add constraint "FK_Campo_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Cuadratura"
-- Especificacion de las campos agrupacion de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.Cuadratura" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo de la cuadratura.
    "Tipo" nvarchar(20) not null,

    -- Nombre del campo.
    "Descripcion" nvarchar(80) not null,

    primary key ("Esquema", "Version", "Tipo")
);

alter table "BSCL.Normativa.BD::DOC.Cuadratura"
   add constraint "FK_Cuadratura_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.CampoCuadratura"
-- Especificacion de los campos agrupacion de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.CampoCuadratura" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo de la cudratura.
    "TipoCuadratura" nvarchar(40) not null,

    -- Nombre del campo.
    "NombreCampo" nvarchar(40) not null,

    -- Orden del campo en la cuadratura.
    "Orden" smallint not null,

    primary key ("Esquema", "Version", "TipoCuadratura", "NombreCampo")
);

alter table "BSCL.Normativa.BD::DOC.CampoCuadratura"
   add constraint "FK_CampoCuadratura_Cuadratura" foreign key ("Esquema", "Version", "TipoCuadratura")
      references "BSCL.Normativa.BD::DOC.Cuadratura" ("Esquema", "Version", "Tipo")
      on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.CampoCuadratura"
   add constraint "FK_CampoCuadratura_Campo" foreign key ("Esquema", "Version", "NombreCampo")
      references "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Clasificador"
-- Especificacion de las campos clasificador de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.Clasificador" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo del clasificador.
    "Tipo" nvarchar(20) not null,

    -- Nombre del campo.
    "Descripcion" nvarchar(80) not null,

    primary key ("Esquema", "Version", "Tipo")
);

alter table "BSCL.Normativa.BD::DOC.Clasificador"
   add constraint "FK_Clasificador_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.CampoClasificador"
-- Especificacion de los campos de clasificador de los detalles de un documento.
create column table "BSCL.Normativa.BD::DOC.CampoClasificador" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Tipo del clasificador.
    "TipoClasificador" nvarchar(40) not null,

    -- Nombre del campo.
    "NombreCampo" nvarchar(40) not null,

    -- Valor del clasificador del campo.
    "ValorClasificador" nvarchar(100) not null,

    primary key ("Esquema", "Version", "TipoClasificador", "NombreCampo")
);

alter table "BSCL.Normativa.BD::DOC.CampoClasificador"
   add constraint "FK_CampoClasificador_Clasificador" foreign key ("Esquema", "Version", "TipoClasificador")
      references "BSCL.Normativa.BD::DOC.Clasificador" ("Esquema", "Version", "Tipo")
      on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.CampoClasificador"
   add constraint "FK_CampoClasificador_Campo" foreign key ("Esquema", "Version", "NombreCampo")
      references "BSCL.Normativa.BD::DOC.Campo" ("Esquema", "Version", "Nombre")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Diccionario"
-- Definicion de codigos utilizados en la edicion y presentacion del documento.
-- Sirve para llenar combos.
-- La nomenclatura o codigo es la impuesta por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Diccionario" (
    -- Tipo de diccionario (actua como clasificador).
    -- Ej. CodigoConcepto, Clasific, CodMoneda
    "Tipo" nvarchar(40) not null,

    -- Codigo del par (Codigo, Descripcion).
    "Codigo" nvarchar(40) not null,

    -- Orden de presentacion en la vista.
    "Orden" smallint not null,

    -- Descripcion del par (Codigo, Descripcion).
    "Descripcion" nvarchar(100) not null,

    -- Indicador de si el registro esta activo (S/N).
    "Activo" char(1) not null,

    -- Objetivo ...
    "Objetivo" nvarchar(100) not null,

    -- Producto del Sistema Operacional al cual aplica.
    "Producto" nvarchar(100) not null,

    primary key ("Tipo", "Codigo")
);

-- Table: "BSCL.Normativa.BD::DOC.Homologacion"
-- Contiene homologación de tipos.
-- Cuando se cargan datos desde un origen operacional se debe aplicar
-- conversiones a los tipos de datos para dejar en formato segun de
-- acuerdo a los codigos definidos por la entidad reguladora.
create column table "BSCL.Normativa.BD::DOC.Homologacion" (
    -- Id del registro.
    "IdHomologacion" bigint not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Nombre del campo a homologar.
    "Tipo" nvarchar(40) not null,

    -- Descripcion del campo a homologar.
    "Descripcion" nvarchar(100) not null,

    -- Valor en el sistema de origen.
    "ValorOrigen" nvarchar(100) not null,

    -- Valor en el sistema de destino.
    "ValorDestino" nvarchar(100) not null,
    
     -- Estado de Registro.
    "IdEstado" nvarchar(10) not null default '',

    primary key ("IdHomologacion")
);

alter table "BSCL.Normativa.BD::DOC.Homologacion"
   add constraint "FK_Homologacion_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Parametro"
-- Contiene los parametros del esquema.
create column table "BSCL.Normativa.BD::DOC.Parametro" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Codigo del parametro.
    "Codigo" nvarchar(40) not null,

    -- Descripcion del parametro.
    "Descripcion" nvarchar(100) not null,

    -- Valor del parametro.
    "Valor" nvarchar(100) not null,

    primary key ("Esquema", "Version", "Codigo")
);

alter table "BSCL.Normativa.BD::DOC.Parametro"
   add constraint "FK_Parametro_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.Validacion"
-- Definición de las validaciones aplicadas a los documentos.
create column table "BSCL.Normativa.BD::DOC.Validacion" (
    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Codigo de identificacion de la validacion (Ej: H1520).
    "Codigo" nvarchar(40) not null,

    -- Tipo de validacion (Ej: NORMA, CUADRATURA, NEGOCIO).
    "Tipo" nvarchar(40) not null,

    -- Indicador de la regla esta activa (S/N).
    "Activa" char(1) not null,

    -- Descripcion de la validacion.
    -- Ej: Si Tipo de Fondo es igual a F, entonces Tipo de Operacion debe ser igual a 1.
    "Descripcion" nvarchar(500) not null,

    -- Severidad de la validacion.
    -- "A" Advertencia / "E" Error.
    "Severidad" char(1) not null,

    -- Expresion de la regla de validacion.
    -- Ej. Si (C2) = F -> (C5) = 1
    "Formula" nvarchar(250) not null,

    primary key ("Esquema", "Version", "Codigo")
);

alter table "BSCL.Normativa.BD::DOC.Validacion"
   add constraint "FK_Validacion_Esquema" foreign key ("Esquema", "Version")
      references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
      on delete restrict on update restrict;

-- Table: "BSCL.Normativa.BD::DOC.TipoDocumento"
-- Definición de Tipo de Documento,
create column table "BSCL.Normativa.BD::DOC.TipoDocumento" (
    -- Nombre del tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Descripcion del tipo de documento.
    -- Ejemplos:
    --     + Certificados por Cliente (Requiere Firma)
    --     + Declaraciones por Corredor (Confirmacion)
    --     + Ciculares por Corredor (Confirmacion)
    --     + Circulares por Cliente (Firma)
    "Descripcion" 				nvarchar(250) not null,
	"IdNormativa" 				NVARCHAR(15)    NOT NULL  DEFAULT '',
	"UI_Visor" 	  				NVARCHAR(2)     NOT NULL  DEFAULT 'N',
	"UI_Emision" 	 	 		NVARCHAR(2)     NOT NULL  DEFAULT 'S',
	"Esquema" 	  				NVARCHAR(10) 	  NOT NULL  DEFAULT '',
	"IdTipoProgramaExportacion" NVARCHAR(40) NOT NULL  DEFAULT '',
	"IdTipoProgramaProceso"  	NVARCHAR(40) NOT NULL  DEFAULT '',
	"ExtensionSalida"  			NVARCHAR(5) NOT NULL  DEFAULT '',
	"ComprimirSalida"  			NVARCHAR(2) NOT NULL  DEFAULT 'N',
	"TitulosSalida"  			NVARCHAR(2) NOT NULL  DEFAULT 'S',
	"LineasPorSalida"  			BIGINT NOT NULL  DEFAULT 0,
    primary key ("IdTipoDocumento")
);
-- Table: "BSCL.Normativa.BD::DOC.TipoDocumento"
-- Definición del Tipo de Documento en Interfaz

CREATE COLUMN TABLE "BSCL.Normativa.BD::DOC.TipoDocumentoUI" (
   "IdTipoDocumento" 				NVARCHAR(10) 	NOT NULL,
   "UI" 	  						NVARCHAR(40)    NOT NULL,
   "IdEstado"		 				NVARCHAR(3)  	NOT NULL DEFAULT 'ACT',
    PRIMARY KEY ("IdTipoDocumento","UI")
);


-- Table: "BSCL"."BSCL.Normativa.BD::DOC.Documento"
-- Contiene la edicion del documento.
-- Cada periodo a informar debe inscribir un registro aca.
create column table "BSCL.Normativa.BD::DOC.Documento" (
    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Tipo de documento.
    "IdTipoDocumento" nvarchar(10) not null,

    -- Nombre del esquema.
    "Esquema" nvarchar(10) not null,

    -- Version del esquema.
    "Version" nvarchar(10) not null,

    -- Id de la empresa.
    "IdEmpresa" bigint not null,

    -- Fecha limite de presentacion del documento.
    -- Que pasa cuando es mensual, se van agregando ediciones mensuales?
    "FechaLimite" date not null,

    -- Estado del documento (debe ser consecuente con Estados del Flujo).
    "IdEstadoDocumento" nvarchar(40) not null,

    -- Fecha de creacion del documento.
    "FechaCreacion" seconddate not null,

    -- Fecha de presentacion del documento.
    "FechaPresentacion" seconddate null,

    -- Documento Legal que identifica al declarante.
    "DocumentoIdentidadDeclarante" nvarchar(40) not null,

    -- Identificador Tributario o Documento Legal que identifica al Representante Legal
    "DocumentoIdentidadRepresentante" nvarchar(40) not null,

    "DireccionElectronicaDeclaracion" nvarchar(100) not null,

    "DireccionFisicaDeclaracion" nvarchar(100) not null,

    "ComunaDeclaracion" nvarchar(100) not null,

    "TelefonoDeclaracion" nvarchar(100) not null,

    -- Folio de Documento utilizado en las interfaces a enviar al regulador.
    -- Ej: I9153103
    "FolioDeclaracion" nvarchar(100) not null,
    
    -- Indica si el documento esta en proceso
    "EnProceso" char(1) not null,

    -- Timestamp del inicio del ultimo proceso.
    "FechaInicioUltimoProceso" datetime null,
	 "AnoMes"  NVARCHAR(6) NOT NULL  DEFAULT '',
    primary key ("IdDocumento")
);

alter table "BSCL.Normativa.BD::DOC.Documento"
    add constraint "FK_Documento_TipoDocumento" foreign key ("IdTipoDocumento")
        references "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento")
        on delete restrict on update restrict;

alter table "BSCL.Normativa.BD::DOC.Documento"
    add constraint "FK_Documento_Esquema" foreign key ("Esquema", "Version")
        references "BSCL.Normativa.BD::DOC.Esquema" ("Esquema", "Version")
        on delete restrict on update restrict;

-- Table: "BSCL"."BSCL.Normativa.BD::TRI.ResumenValidacion"
-- Contiene los resumenes de validacion del documento.
create column table "BSCL.Normativa.BD::TRI.ResumenValidacion" (
    -- Id del registro.
    "ID" bigint not null,

    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Id del proceso.
    "IdProceso" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Codigo de la validacion (Ej: H1520).
    "CodigoValidacion" nvarchar(40) not null,

    -- Descripcion de la validacion.
    "DescripcionValidacion" nvarchar(250) not null,

    -- Cantidad de casos en que no se cumple la regla.
    "CantidadCasos" bigint not null,

    -- Fecha de la validacion.
    "FechaValidacion" date not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenValidacion"
    add constraint "FK_ResumenValidacion_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

-- Table: "BSCL"."BSCL.Normativa.BD::TRI.DetalleValidacion"
-- Contiene los detalles de validacion del documento.
create column table "BSCL.Normativa.BD::TRI.DetalleValidacion" (
    -- Id del registro.
    "ID" bigint not null,

    -- Id del resumen de validacion.
    "IdResumenValidacion" bigint not null,

    -- Id del detalle del documento.
    "IdDetalleDocumento" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DetalleValidacion"
    add constraint "FK_DetalleValidacion_ResumenValidacion" foreign key ("IdResumenValidacion")
        references "BSCL.Normativa.BD::TRI.ResumenValidacion" ("ID")
        on delete cascade on update restrict;

create column table "BSCL.Normativa.BD::TRI.ResumenCarga" (
    -- Id del resumen de carga.
    "ID" bigint not null,

    -- Id del proceso.
    "IdProceso" bigint not null,

    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Fecha/Hora de la auditoria.
    "Fecha" datetime not null,

    -- Glosa de la auditoria.
    "Glosa" nvarchar(200) not null,

    -- Nombre del archivo.
    "NombreArchivo" nvarchar(200) null,

    -- Total de registros leidos.
    "TotalRegistrosLeidos" bigint not null,

    -- Total de registros cargados.
    "TotalRegistrosCargados" bigint not null,

    -- Total de registros erroneos.
    "TotalRegistrosErroneos" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenCarga"
    add constraint "FK_ResumenCarga_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

create column table "BSCL.Normativa.BD::TRI.ResumenGeneracion" (
    -- Id del resumen de generacion.
    "ID" bigint not null,

    -- Id del proceso.
    "IdProceso" bigint not null,

    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Fecha/Hora de la generacion.
    "Fecha" datetime not null,

    -- Glosa del resultado de la generacion.
    "Glosa" nvarchar(200) not null,

    -- Total de registros leidos.
    "TotalRegistrosGenerados" bigint not null,

    -- Nombre del archivo resultado de la generacion.
    "NombreArchivo" nvarchar(200) default null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenGeneracion"
    add constraint "FK_ResumenGeneracion_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

create column table "BSCL.Normativa.BD::TRI.ResumenCuadratura" (
    -- Id del resumen de cuadratura.
    "ID" bigint not null,

    -- Id del proceso.
    "IdProceso" bigint not null,

    -- Id del documento.
    "IdDocumento" bigint not null,

    -- Id del usuario.
    "IdUsuario" bigint not null,

    -- Fecha/Hora de la cuadratura.
    "FechaCuadratura" datetime not null,

    -- Ano/Mes de los datos de la cuadratura.
    "AnoMes" datetime not null,

    -- Tipo de cuadratura.
    "TipoCuadratura" nvarchar(20) not null,

    -- Tipo de clasificador.
    "TipoClasificador" nvarchar(20) null,

    -- Tipo de diferencia.
    "TipoDiferencia" nvarchar(20) not null,

    -- Glosa de la cuadratura.
    "Glosa" nvarchar(200) not null,

    -- Cantidad de casos en que no se cumple la cuadratura.
    "CantidadCasos" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.ResumenCuadratura"
    add constraint "FK_ResumenCuadratura_Documento" foreign key ("IdDocumento")
        references "BSCL.Normativa.BD::DOC.Documento" ("IdDocumento")
        on delete cascade on update restrict;

-- Table: "BSCL"."BSCL.Normativa.BD::TRI.DetalleCuadratura"
-- Contiene los detalles de cuadratura del documento.
create column table "BSCL.Normativa.BD::TRI.DetalleCuadratura" (
    -- Id del detalle de cuadratura.
    "ID" bigint not null,

    -- Id del resumen de cuadratura.
    "IdResumenCuadratura" bigint not null,

    -- Id del detalle del documento.
    "IdDetalleDocumento" bigint not null,

    primary key ("ID")
);

alter table "BSCL.Normativa.BD::TRI.DetalleCuadratura"
    add constraint "FK_DetalleCuadratura_ResumenCuadratura" foreign key ("IdResumenCuadratura")
        references "BSCL.Normativa.BD::TRI.ResumenCuadratura" ("ID")
        on delete cascade on update restrict;

/*==============================================================*/
/* PARAMETROS DE CONFIGURACION                                  */
/*==============================================================*/

create column table "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor" (
	"IdMantenedor"				nvarchar(40) NOT NULL,
	"IdTipoDocumento" 			nvarchar(10) NOT NULL,
	"IdObjeto"					NVARCHAR (100) NOT NULL,
	"ValorActivo"				CHAR (1) NOT NULL DEFAULT 'N',
	
	primary key ("IdMantenedor", "IdTipoDocumento", "IdObjeto")
);

alter table "BSCL.Normativa.BD::DOC.ConfiguracionMantenedor"
   add constraint "FK_ConfiguracionMantenedor_TipoDocumento" foreign key ("IdTipoDocumento")
      references "BSCL.Normativa.BD::DOC.TipoDocumento" ("IdTipoDocumento")
      on delete restrict on update restrict;
