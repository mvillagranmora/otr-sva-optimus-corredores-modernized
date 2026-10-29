set schema "BSCL";

/*==============================================================*/
/* Create Table:  "BSCL.Normativa.BD::TRI.DatosDJ1922"          */
/*==============================================================*/
/*
 Contiene los datos de la DatosDJ1922.
 Permite el ingreso de datos nulos, pues no en todas las ediciones del documento se informan todos los campos o existen campos condicionados entre si.
*/
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
        
-- create index "IX01_DocumentoDJ1885" on "BSCL.Normativa.BD::TRI.DatosDJ1885"  ("IdDocumento", "TipoInstrumento", "SecuenciaEvento");    

/*==============================================================*/
/* Delete Table:                                                */
/*==============================================================*/
delete from "BSCL"."BSCL.Normativa.BD::TRI.DatosDJ1922"; 
delete from "BSCL"."BSCL.Normativa.BD::DOC.Documento" 			where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Validacion" 			where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Homologacion" 		where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Parametro" 		    where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura" 	where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura" 			where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador" 	where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Clasificador" 		where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Campo" 				where "Esquema" = 'DJ1922';
delete from "BSCL"."BSCL.Normativa.BD::DOC.Esquema" 			where "Esquema" = 'DJ1922';

/*==============================================================*/
/* ESQUEMAS                                                     */
/*==============================================================*/

insert into "BSCL"."BSCL.Normativa.BD::DOC.Esquema"
select
    'DJ1922' as "Esquema",
    '2017' as "Version",
    'A' as "Periodicidad",
    '' as "Objetivo",
    '1996-02-15' as "PeriodoInicio",
    'SII' as "IdEntidadReguladora",
    '2017-03-26' as "FechaPresentacionLimite",
    '2016-01-01' as "FechaDatosDesde",
    '2016-12-31' as "FechaDatosHasta",
    14 as "CodigoVersionSII"
from dummy;

/*==============================================================*/
/* CAMPOS                                                       */
/*==============================================================*/
--
-- DJ18922-2017
--
DELETE   FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1922';
SELECT * FROM "BSCL"."BSCL.Normativa.BD::DOC.Campo" WHERE "Esquema" = 'DJ1922';

insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ID' as "Nombre",'' as "TipoDiccionario",1 as "Orden",'Secuencia' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdDocumento' as "Nombre",'' as "TipoDiccionario",2 as "Orden",'Secuencia Docto.' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'S' as "OcultoEnGrilla",'S' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'VboUsuario' as "Nombre",'VistoBueno' as "TipoDiccionario",3 as "Orden",'VoBo' as "Titulo",'TEXT' as "TipoDato",1 as "Largo",'N' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'Observaciones' as "Nombre",'' as "TipoDiccionario",4 as "Orden",'Observaciones' as "Titulo",'TEXT' as "TipoDato",250 as "Largo",'' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'FuenteDatos' as "Nombre",'' as "TipoDiccionario",5 as "Orden",'Fuente' as "Titulo",'TEXT' as "TipoDato",10 as "Largo",'INICIAL' as "ValorDefecto",'N' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'S' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'RutCliente' as "Nombre",'' as "TipoDiccionario",6 as "Orden",'RUT DEL PARTICIPE O APORTANTE' as "Titulo",'RUT_CLIENTE' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'' as "Grupo",1 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdCliente' as "Nombre",'' as "TipoDiccionario",7 as "Orden",'Secuencia Cliente' as "Titulo",'ID_CLIENTE' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdPortafolio' as "Nombre",'' as "TipoDiccionario",8 as "Orden",'Secuencia Portafolio' as "Titulo",'ID_PORTAFOLIO' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'FechaEvento' as "Nombre",'' as "TipoDiccionario",9 as "Orden",'Fecha de Movimiento' as "Titulo",'DATE_FILTER' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",2 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'RutAdministradora' as "Nombre",'' as "TipoDiccionario",10 as "Orden",'RUT DE SOC. ADMINISTRADORA' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",3 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'RutFondo' as "Nombre",'' as "TipoDiccionario",11 as "Orden",'RUT DEL FONDO/RUN DEL FONDO MUTUO ' as "Titulo",'RUT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'N' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",4 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'Nemotecnico' as "Nombre",'' as "TipoDiccionario",12 as "Orden",'NEMOTECNICO' as "Titulo",'NEMO' as "TipoDato",100 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",5 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TipoFondo' as "Nombre",'TipoFondo' as "TipoDiccionario",13 as "Orden",'TIPO DE FONDO' as "Titulo",'NEMO' as "TipoDato",100 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",6 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'NombreParticipe' as "Nombre",'' as "TipoDiccionario",14 as "Orden",'NOMBRE O RAZÓN SOCIAL DEL PARTICIPE O APORTANTE' as "Titulo",'TEXT' as "TipoDato",250 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",7 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TaxId' as "Nombre",'' as "TipoDiccionario",15 as "Orden",'TAX-ID' as "Titulo",'TEXT' as "TipoDato",250 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",8 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CodigoPais' as "Nombre",'CodigoPais' as "TipoDiccionario",16 as "Orden",'CODIGO PAIS DE RESIDENCIA DEL PARTICIPE O APORTANTE' as "Titulo",'NEMO' as "TipoDato",100 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",9 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TipoOperacion' as "Nombre",'TipoOperacionFondos' as "TipoDiccionario",17 as "Orden",'TIPO DE OPERACIÓN' as "Titulo",'NEMO' as "TipoDato",100 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'1. DATOS DE LOS INFORMADOS' as "Grupo",10 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'FechaInversion' as "Nombre",'' as "TipoDiccionario",18 as "Orden",'FECHA DE LA INVERSIÓN' as "Titulo",'DATE_FILTER' as "TipoDato",0 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'N' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",11 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'MonedaReajuste' as "Nombre",'TipoReajuste' as "TipoDiccionario",19 as "Orden",'MONEDA O UNIDAD DE REAJUSTE' as "Titulo",'NEMO' as "TipoDato",100 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",12 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'MontoNominalAportes' as "Nombre",'' as "TipoDiccionario",20 as "Orden",'MONTO NOMINAL DE  APORTES AL FONDO EN               $' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",13 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'NumeroSerieCuotas' as "Nombre",'' as "TipoDiccionario",21 as "Orden",'NÚMERO DE SERIE DE CUOTAS DE PROPIEDAD DEL PARTÍCIPE O APORTANTE' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",14 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CantidadCuotasParticipe' as "Nombre",'' as "TipoDiccionario",22 as "Orden",'NUMERO DE CUOTAS DE LA SERIE DE PROPIEDAD DEL PARTÍCIPE O APORTANTE' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",15 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ParticipeCumpleRequisitos' as "Nombre",'ParticipeCumpleRequisitos' as "TipoDiccionario",23 as "Orden",'PARTÍCIPE CUMPLE REQUISITOS (Art.82 N°1  letra B) iii), Ley Adm.Fondos contenida Art. 1° Ley 20.712)' as "Titulo",'ID' as "TipoDato",0 as "Largo",'1' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",16 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CuotasCumplenRequisitos' as "Nombre",'CuotasCumplenRequisitos' as "TipoDiccionario",24 as "Orden",'CUOTAS DE FONDOS QUE CUMPLEN REQUISITOS Art.82' as "Titulo",'ID' as "TipoDato",0 as "Largo",'1' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",17 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ValorCuotaPesos' as "Nombre",'' as "TipoDiccionario",25 as "Orden",'VALOR DE LA CUOTA ($)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",18 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'PorcentajeParticipacion31Dic' as "Nombre",'' as "TipoDiccionario",26 as "Orden",'% DE PARTICIPACIÓN EN EL PATRIMONIO DEL FONDO AL 31.12' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'2. INFORMACIÓN DE LOS APORTES AL FONDO' as "Grupo",19 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ValNominalCuotasRescatadas' as "Nombre",'' as "TipoDiccionario",27 as "Orden",'VALOR NOMINAL CUOTAS RESCATADAS O ENAJENADAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",20 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CantidadCuotasRescatadas' as "Nombre",'' as "TipoDiccionario",28 as "Orden",'CANTIDAD DE CUOTAS RESCATADAS O ENAJENADAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",21 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DiferenciaPorRescateOEnajenacion' as "Nombre",'' as "TipoDiccionario",29 as "Orden",'DIFERENCIA OBTENIDA EN EL RESCATE O ENAJENACIÓN DE CUOTAS (ACTUALIZADA)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",22 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DifPorRescateOEnajenacionCFIAcogido107' as "Nombre",'' as "TipoDiccionario",30 as "Orden",'DIFERENCIA EN EL RESCATE DE CUOTAS DE FONDOS DE INVERSIÓN ART.107 (ACTUALIZADA)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",23 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ValNominalInversionaFechaLiquidacion' as "Nombre",'' as "TipoDiccionario",31 as "Orden",'VALOR NOMINAL INVERSIÓN A LA FECHA DE LIQUIDACIÓN O DISMINUCIÓN DE CAPITAL' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",24 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CantCuotasCedidasPorLiquidacion' as "Nombre",'' as "TipoDiccionario",32 as "Orden",'CUOTAS ENAJENADAS LIQUIDACIÓN DEL FONDO O RESCATADAS POR DISMINUCIÓN DE CAPITAL' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",25 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DiferenciaValorCuotasFechaLiquidacion' as "Nombre",'' as "TipoDiccionario",33 as "Orden",'DIFERENCIA VALOR DE CUOTAS A FECHA LIQUIDACIÓN V/S VALOR A FECHA DE ADQUISICIÓN (ACTUALIZADO)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'3. DETALLE DE OPERACIONES Y MOVIMIENTOS' as "Grupo",26 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosAfectosIGCoAdicional' as "Nombre",'' as "TipoDiccionario",34 as "Orden",'DIVIDENDOS  REMESAS O DISTRIBUCIONES AFECTAS A G. COMPLEMENTARIO O ADICIONAL ' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",27 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosExentosIGCoAdicional' as "Nombre",'' as "TipoDiccionario",35 as "Orden",'DIVIDENDOS  REMESAS  O DISTRIBUCIONES EXENTAS DE GLOBAL COMPLEMENTARIO O ADICIONAL' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",28 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosNoConstitutivosDeRenta' as "Nombre",'' as "TipoDiccionario",36 as "Orden",'DIVIDENDOS REMESAS O DISTRIBUCIONES NO CONSTITUTIVOS DE RENTA' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",29 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosAfectosImptoUnicoTasa10' as "Nombre",'' as "TipoDiccionario",37 as "Orden",'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO ÚNICO TASA 10%' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",30 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosNoAfectosImptoUnicoTasa10' as "Nombre",'' as "TipoDiccionario",38 as "Orden",'DISTRIBUCIONES  O REMESAS NO AFECTAS A IMPUESTO ÚNICO TASA 10%' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",31 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DividendosAfectosImptoUnicoTasa4' as "Nombre",'' as "TipoDiccionario",39 as "Orden",'DISTRIBUCIONES  O REMESAS AFECTAS A IMPUESTO ÚNICO TASA 4%' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",32 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DistribucionXDiminucionNoImputadaCapital' as "Nombre",'' as "TipoDiccionario",40 as "Orden",'DISTRIBUCIONES EFECTUADAS MEDIANTE LA DISMINUCIÓN DEL VALOR DE CUOTA DEL FONDO NO IMPUTADA A CAPITAL' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",33 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'DevolucionesDeCapital' as "Nombre",'' as "TipoDiccionario",41 as "Orden",'DEVOLUCIONES DE CAPITAL' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",34 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IncrementoPorImpuesto' as "Nombre",'' as "TipoDiccionario",42 as "Orden",'INCREMENTO POR IMPUESTO PRIMERA CATEGORÍA' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'4. MONTO DE DISTRIBUCIONES  O REMESAS Y DEVOLUCIONES DE CAPITAL, REAJUSTADOS ($)' as "Grupo",35 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CreditoImpto1CatConDevolucionAfectas' as "Nombre",'' as "TipoDiccionario",43 as "Orden",'CRÉDITO 1° CAT. CON DERECHO A DEVOLUCIÓN POR RENTAS AFECTAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",36 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CreditoImpto1CatSinDevolucionAfectas' as "Nombre",'' as "TipoDiccionario",44 as "Orden",'CRÉDITO 1° CAT. SIN DERECHO A DEVOLUCIÓN POR RENTAS AFECTAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",37 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CreditoImpto1CatConDevolucionExentas' as "Nombre",'' as "TipoDiccionario",45 as "Orden",'CRÉDITO 1° CAT. CON DERECHO A DEVOLUCIÓN POR RENTAS EXENTAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",38 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CreditoImpto1CatSinDevolucionExentas' as "Nombre",'' as "TipoDiccionario",46 as "Orden",'CRÉDITO 1° CAT. SIN DERECHO A DEVOLUCIÓN POR RENTAS EXENTAS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",39 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'CreditoPorImpuestosExternos' as "Nombre",'' as "TipoDiccionario",47 as "Orden",'CRÉDITO POR IMPUESTOS EXTERNOS' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",40 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'ImpuestosTasaAdicionalExArticulo21' as "Nombre",'' as "TipoDiccionario",48 as "Orden",'IMPTO. TASA ADICIONAL EX. ART. 21 L.I.R. ($)' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",41 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'MontoInversionenCuotasCFIAnterior93' as "Nombre",'' as "TipoDiccionario",49 as "Orden",'MONTO INVERSIÓN EN CUOTAS DE FONDOS DE INVERSIÓN LEY N° 18.815' as "Titulo",'AMOUNT' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",42 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'NumeroCertificado' as "Nombre",'' as "TipoDiccionario",50 as "Orden",'NÚMERO CERT.' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'N' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'5. CRÉDITOS PARA G. COMPLEMENTARIO  O ADICIONAL' as "Grupo",43 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdInstrumento' as "Nombre",'' as "TipoDiccionario",51 as "Orden",'Instrumento' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdEventoCapitalCliente' as "Nombre",'' as "TipoDiccionario",52 as "Orden",'Evento de Capital' as "Titulo",'ID' as "TipoDato",0 as "Largo",'0' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TipoSocioNegocio' as "Nombre",'' as "TipoDiccionario",53 as "Orden",'Tipo Socio de Negocio' as "Titulo",'NEMO' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TipoCartera' as "Nombre",'' as "TipoDiccionario",54 as "Orden",'Tipo de Cartera' as "Titulo",'NEMO' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'IdMoneda' as "Nombre",'' as "TipoDiccionario",55 as "Orden",'Moneda' as "Titulo",'NEMO' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'N' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Campo" Select 'DJ1922' as "Esquema",'2017' as "Version",'TipoEventoCapital' as "Nombre",'TipoEventoCapital' as "TipoDiccionario",56 as "Orden",'Tipo Evento de Capital' as "Titulo",'NEMO' as "TipoDato",40 as "Largo",'' as "ValorDefecto",'S' as "Obligatorio",'S' as "Editable",'S' as "ModoMasivo",'S' as "Exportable",'N' as "OcultoEnGrilla",'N' as "OcultoEnMantenedor",'N' as "Comun",'S' as "Clave",'6. DATOS DE CONTROL' as "Grupo",0 as "OrdenCargaExterna" FROM DUMMY;

/*==============================================================*/
/* HOMOLOGACIONES                                               */
/*==============================================================*/
insert into "BSCL"."BSCL.Normativa.BD::DOC.Homologacion"
select 	1 as "IdHomologacion", 'DJ1922' as "Esquema", '2017' as "Version", 'TipoReajuste' as "Tipo",  'Tipo Reajuste' as "Descripcion" , '1' as  "ValorOrigen", 'CLP' as  "ValorDestino" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Homologacion"
select 	2 as "IdHomologacion", 'DJ1922' as "Esquema", '2017' as "Version", 'TipoReajuste' as "Tipo",  'Tipo Reajuste' as "Descripcion" , '2' as  "ValorOrigen", 'USD' as  "ValorDestino" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Homologacion"
select 	3 as "IdHomologacion", 'DJ1922' as "Esquema", '2017' as "Version", 'TipoReajuste' as "Tipo",  'Tipo Reajuste' as "Descripcion" , '3' as  "ValorOrigen", 'EUR' as  "ValorDestino" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Homologacion"
select 	4 as "IdHomologacion", 'DJ1922' as "Esquema", '2017' as "Version", 'TipoReajuste' as "Tipo",  'Tipo Reajuste' as "Descripcion" , '4' as  "ValorOrigen", 'UF' as  "ValorDestino" from dummy;
insert into "BSCL"."BSCL.Normativa.BD::DOC.Homologacion"
select 	5 as "IdHomologacion", 'DJ1922' as "Esquema", '2017' as "Version", 'TipoReajuste' as "Tipo",  'Tipo Reajuste' as "Descripcion" , '5' as  "ValorOrigen", 'IVP' as  "ValorDestino" from dummy;


/*==============================================================*/
/* DOCUMENTOS : app actualiza datos del declarante              */
/*==============================================================*/
select * from "BSCL"."BSCL.Normativa.BD::DOC.Documento"
insert into "BSCL"."BSCL.Normativa.BD::DOC.Documento"
select
    3 as "IdDocumento",
    'DECLACORR' as "IdTipoDocumento",
    'DJ1922' as "Esquema",
    '2017' as "Version",
    1 as "IdEmpresa",
    '2017-03-26' as "FechaLimite",
    'ACT' as "IdEstadoDocumento",
    '2017-01-01' as "FechaCreacion",
    '2017-03-26' as "FechaPresentacion",
    '90947000-1' as "DocumentoIdentidadDeclarante",
    '14448678-1' as "DocumentoIdentidadRepresentante",
    'contabilidad@imtrust.cl' as "DireccionElectronicaDeclaracion",
    'El Golf 47' as "DireccionFisicaDeclaracion",
    'Las Condes' as "ComunaDeclaracion",
    '5689854' as "TelefonoDeclaracion",
    'I9153103' as "FolioDeclaracion",
    'N' as "EnProceso",
    null as "EnProcesoDesde"
from dummy;

/*==============================================================*/
/* VALIDACIONES                                                 */
/*==============================================================*/
/*
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1922' as "Esquema",
    '2017' as "Version",
    'H115' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'N° de documento > 0' as "Descripcion",
    'E' as "Severidad",
    'N° de documento > 0' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1922' as "Esquema",
    '2017' as "Version",
    'H116' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Descripcion",
    'E' as "Severidad",
    'Tipo Documento= {F .o. C .o. D .o. O}' as "Formula"
from dummy;

insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H117' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Tipo Transacción={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Descripcion",
    'E' as "Severidad",
    'Tipo Transacción={0.o.1.o.2.o. 3.o. 4.o. 5.o. 6.o. 7.o.8.o.9 .o.10.o. 99}' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H1350' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Descripcion",
    'E' as "Severidad",
    'Si C8 = 1.o. C8 = 8, entonces [C13] <> blanco. O. [C13] > 0' as "Formula"
from dummy;
insert into "BSCL.Normativa.BD::DOC.Validacion"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'H611' as "Codigo",
    'NORMA' as "Tipo",
    'S' as "Activa",
    '31.12.AT-2 < fecha < 01.01.AT .y. fecha <> Blanco' as "Descripcion",
    'E' as "Severidad",
    '31.12.AT-2 < fecha < 01.01.AT .y. fecha <> Blanco' as "Formula"
from dummy;
*/
/*==============================================================*/
/* CUADRATURAS : Criterio de Agrupamiento                       */
/*==============================================================*/
/*
insert into "BSCL"."BSCL.Normativa.BD::DOC.Cuadratura"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorRutCliente' as "Tipo",
    'Cuadratura por RUT Cliente' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoCuadratura"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorRutCliente' as "TipoCuadratura",
    'RutCliente' as "NombreCampo",
    1 as "Orden"
from dummy;
*/
/*==============================================================*/
/* CLASIFICADORES : Criterio de Grupo de Datos : Totaliza por este criterio, Facturas de Venta, Facturas de Compra, son grupos de datos separados */
/*==============================================================*/
/*
insert into "BSCL"."BSCL.Normativa.BD::DOC.Clasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorFacturasCompras' as "Tipo",
    'Facturas de Compra' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorFacturasCompras' as "TipoClasificador",
    'TipoDocumento' as "NombreCampo",
    'F' as "ValorClasificador"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorFacturasCompras' as "TipoClasificador",
    'IdTipoOperacion' as "NombreCampo",
    'CO' as "ValorClasificador"
from dummy;



insert into "BSCL"."BSCL.Normativa.BD::DOC.Clasificador"
select
    'DJ1891' as "Esquema",
    '2017' as "Version",
    'PorFacturasVentas' as "Tipo",
    'Facturas de Venta' as "Descripcion"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891' 			as "Esquema",
    '2017' 				as "Version",
    'PorFacturasVentas' as "TipoClasificador",
    'TipoDocumento' 	as "NombreCampo",
    'F' 				as "ValorClasificador"
from dummy;

insert into "BSCL"."BSCL.Normativa.BD::DOC.CampoClasificador"
select
    'DJ1891'            as "Esquema",
    '2017'              as "Version",
    'PorFacturasVentas' as "TipoClasificador",
    'IdTipoOperacion'   as "NombreCampo",
    'VE'                as "ValorClasificador"
from dummy;

*/
/*==============================================================*/
/* Commit:                                                      */
/*==============================================================*/

commit;
