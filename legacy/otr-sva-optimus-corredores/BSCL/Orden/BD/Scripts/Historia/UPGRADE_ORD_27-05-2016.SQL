-- ########################################################################################
--
--  =========================================================
--  ESTE ARCHIVO CONTIEN LOS SCRIPT HISTORICOS DE LIBERACION
--  =========================================================
--  * Debe incluir los ALTER de Tablas
--  * Debe incluir nuevas tablas, indices, secuencuias, etc.
--  * Debe incluir insert de datos maestros necesarios para el funcionamiento 
--  NOTA: Este archivo no reemplaza los scripts de creación que deben ir en cada módulo.
-- 
-- ########################################################################################

SET SCHEMA BSCL;
--
--
INSERT INTO "BSCL"."BSCL.Plataforma.BD::PLA.Servicio" ( "IdServicio","RutaServicio","UsoServicio","TipoServicio","Descripcion" ) 
VALUES(337,'BSCL.Orden.BD.Servicios::ObtenerDatosDesdeArchivoOPA',0,'P','');

insert into "BSCL.Plataforma.BD::PLA.Archivo"
select (select max("IdArchivo") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.Archivo") as "IdArchivo",1 as "IdEmpresa",'ARCHIVO_ASIGNACION_OPA' as "CodigoArchivo",'N' as "TieneSeparador",'' as "SeparadorColumnas" 
from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
select (select max("IdFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.Formato") as "IdFormato",
'OPARUT' as "CodigoCampo",'STRING' as "TipoDato", 0 as "Posicion", 11 as "LargoCampo",'S'  as "EsObligatorio", '' as "Validacion", '' as "MensajeValidacion"
from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
select (select max("IdFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.Formato") as "IdFormato",
'OPACODIGOPORTAFOLIO' as "CodigoCampo",'STRING' as "TipoDato", 1 as "Posicion", 2 as "LargoCampo",'S'  as "EsObligatorio", '' as "Validacion", '' as "MensajeValidacion"
from dummy;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.Formato"
select (select max("IdFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.Formato") as "IdFormato",
'OPACANTIDAD' as "CodigoCampo",'NUMBER' as "TipoDato", 2 as "Posicion", 16 as "LargoCampo",'S'  as "EsObligatorio", '' as "Validacion", '' as "MensajeValidacion"
from dummy;


insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
select (select max("IdArchivoFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato") as "IdArchivoFormato","IdArchivo","IdFormato" 
from "BSCL"."BSCL.Plataforma.BD::PLA.Formato" form,"BSCL"."BSCL.Plataforma.BD::PLA.Archivo" arc 
where arc."CodigoArchivo" = 'ARCHIVO_ASIGNACION_OPA' and form."CodigoCampo" = 'OPARUT' order by "IdFormato" desc;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
select (select max("IdArchivoFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato") as "IdArchivoFormato","IdArchivo","IdFormato" 
from "BSCL"."BSCL.Plataforma.BD::PLA.Formato" form,"BSCL"."BSCL.Plataforma.BD::PLA.Archivo" arc 
where arc."CodigoArchivo" = 'ARCHIVO_ASIGNACION_OPA' and form."CodigoCampo" = 'OPACODIGOPORTAFOLIO' order by "IdFormato" desc;

insert into "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato"
select (select max("IdArchivoFormato") +1 from "BSCL"."BSCL.Plataforma.BD::PLA.ArchivoFormato") as "IdArchivoFormato","IdArchivo","IdFormato" 
from "BSCL"."BSCL.Plataforma.BD::PLA.Formato" form,"BSCL"."BSCL.Plataforma.BD::PLA.Archivo" arc 
where arc."CodigoArchivo" = 'ARCHIVO_ASIGNACION_OPA' and form."CodigoCampo" = 'OPACANTIDAD' order by "IdFormato" desc;


-- ########################################################################################
-- ####################### CAR$GA TRANSACCIONES (PLTRAN) - CHERNANDEZ######################
-- ########################################################################################
/*DROP TABLE "BSCL"."BSCL.Orden.BD::ORD.CargaTransaccionesLog" CASCADE;
CREATE COLUMN TABLE "BSCL"."BSCL.Orden.BD::ORD.CargaTransaccionesLog" (
   "IdCargaTransaccionLog"  	BIGINT				NOT NULL,
   "Folio"         				NVARCHAR(40)		NOT NULL,
   "FechaCargaTransaccionLog"   NVARCHAR(26)		NOT NULL,
   "Mercado"					NVARCHAR(2)			NOT NULL, --RV, RF, IF
   "Tipo"          				NVARCHAR(3)			NOT NULL, --MOD, ANU
   "Procesado"					NVARCHAR(1)			NOT NULL, --S, N
   "Estado"		 				NVARCHAR(3)			NOT NULL, --OK, NOK
   primary key ("IdCargaTransaccionLog")
);
DROP SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::CargaTransaccionesLog"      CASCADE;
CREATE SEQUENCE "BSCL"."BSCL.Orden.BD.Secuencias::CargaTransaccionesLog" 
       START     WITH 1
       INCREMENT BY 1
       MINVALUE  1;*/
