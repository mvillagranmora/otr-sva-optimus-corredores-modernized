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
-- LIBERACION SEMANA: 18-03-2016
--

--################################################
--       JG.Preferencias Pago
--################################################

/* POR.portafolio -< POR.PreferenciaPagoPortafolio >- PAG.PreferenciaPago */
create column table "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
	"IdPreferenciaPagoPortafolio"	BIGINT			not null, 
	"IdPortafolio" 					BIGINT 			not null default 0,
	"IdPreferenciaPago"				NVARCHAR(40)	not null default '', 
	"IdPersonaRelacionada"			BIGINT			not null default 0,   -- cuando PreferenciaPago.UsaPersonaRelacionada = 'S'
	"IdCuentaBancaria"				BIGINT			not null default 0, -- cuando PreferenciaPago.UsaCuentaBancaria = 'S'
   	"FechaCreacion" 				SECONDDATE  	not null default '0001-01-01 00:00:00',
	"FechaModificacion" 			SECONDDATE 		not null default '0001-01-01 00:00:00',
	"IdUsuarioCreador"  			BIGINT  		not null default 0,
	"IdEstado"						NVARCHAR(3)   	not null default 'ACT', -- 'ACT','INA'
	"IdEmpresa"						BIGINT         	not null default 0,
	primary key ("IdPreferenciaPagoPortafolio")
);

create sequence "BSCL.Portafolio.BD.Secuencias::PreferenciaPagoPortafolio" increment by 1 start with 10000 minvalue 10000 maxvalue 4611686018427387903 no cycle;


/*-- INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
 	"IdPreferenciaPagoPortafolio","IdPortafolio","IdPreferenciaPago","IdPersonaRelacionada","IdCuentaBancaria","IdEmpresa")
 VALUES(1,132680,'COMACN_EFE_CLP',0,0,1);
 
 INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
 	"IdPreferenciaPagoPortafolio","IdPortafolio","IdPreferenciaPago","IdPersonaRelacionada","IdCuentaBancaria","IdEmpresa")
 VALUES(2,132680,'COMACN_CHE_CLP',0,0,1);
 
 INSERT INTO "BSCL.Portafolio.BD::POR.PreferenciaPagoPortafolio" (
 	"IdPreferenciaPagoPortafolio","IdPortafolio","IdPreferenciaPago","IdPersonaRelacionada","IdCuentaBancaria","IdEmpresa")
 VALUES(3,132680,'VENACN_TRA_CLP',0,11,1);*/
	 	 
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP("IdPerfilFormasPago");
ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" DROP("IdPerfilFormasPagoExtra");

--------------------------------------------------
--      FIN Preferencias Pago
--------------------------------------------------

--################################################
--       JG.Preferencias Facturación
--################################################

SET SCHEMA BSCL;

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
DROP("IdPerfilFacturacion");

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
DROP("IdPerfilFacturacionExtra");

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("IdModeloFacturacionBase" NVARCHAR(40) NOT NULL DEFAULT '0'); -- Sólo Referencia Inicial

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("SeparaFacturaPorTransaccion" NVARCHAR(1) NOT NULL DEFAULT 'N');

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("SeparaFacturaPorInstrumento" NVARCHAR(1) NOT NULL DEFAULT 'N');

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("FusionaFacturaPorPrecio" NVARCHAR(1) NOT NULL DEFAULT 'N');

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("IdModoFacturacion" NVARCHAR(40) NOT NULL DEFAULT 'NORMAL'); -- NORMAL, CONVENIO, CONTRATO

ALTER TABLE "BSCL.Portafolio.BD::POR.Portafolio" 
ADD("IdEntidadModoFacturacion" BIGINT NOT NULL DEFAULT 0);  

--------------------------------------------------
--      FIN Preferencias Facturación
--------------------------------------------------


--################################################
--       <INICIALES DESARROLLADOR>.<MOTIVO>
--################################################

-- PEW PEW!

--------------------------------------------------
--      FIN <MOTIVO>
--------------------------------------------------
