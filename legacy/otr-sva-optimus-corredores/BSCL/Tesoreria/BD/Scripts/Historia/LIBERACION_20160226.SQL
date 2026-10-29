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

SET schema BSCL;
--
--
-- LIBERACION SEMANA: 26-02-2016
--
--################################################
--       	  	 INFORME DE CLEARING
--################################################

-- Parámetros de Configuración
INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeRFPM','Clearing Financiero incluye: RF PM','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeIFPM','Clearing Financiero incluye: IF PM','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeRFPH','Clearing Financiero incluye: RF PH','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'ClearingIncluyeIFPH','Clearing Financiero incluye: IF PH','S');

INSERT INTO "BSCL.Tesoreria.BD::TES.ConfiguracionTesoreria"("IdEmpresa","CodigoParametro","Descripcion", "Valor")
VALUES (1,'CambiosCCLV','Indica si se usan cámaras migradas de CCLV (S:RV/PM/PH+SM)(N:RV+PM/PH+SM)','S');


alter table "BSCL.Tesoreria.BD::TES.Chequera"
add ("IdConfiguracionChequera" BIGINT);

alter table "BSCL.Tesoreria.BD::TES.Chequera"
add constraint "FK_ConfiguracionChequera_Chequera" foreign key ("IdConfiguracionChequera")
references "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" ("IdConfiguracionChequera") on delete restrict on update restrict;



/*==============================================================*/
/* constraint:                                                  */
/*==============================================================*/
/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Caja"
	   add constraint  FK_Empresa_Caja foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda" 
	   add constraint  FK_Caja_CajaMoneda foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;
	
	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda" 
	   add constraint  FK_Moneda_CajaMoneda foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaMoneda"
	   add constraint  FK_Estado_CajaMoneda foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
	   add constraint  FK_Caja_CajaUsuario foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario" 
	   add constraint  FK_Usuario_CajaUsuario foreign key ("IdUsuario")
	      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaUsuario"
	   add constraint  FK_Estado_CajaUsuario foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_Caja_CajaProductoEmpresa foreign key ("IdCaja")
	      references "BSCL.Tesoreria.BD::TES.Caja"  ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa" 
	   add constraint  FK_ProductoEmpresa_CajaProductoEmpresa foreign key ("IdProductoEmpresa")
	      references "BSCL.Plataforma.BD::INS.ProductoEmpresa" ("IdProductoEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CajaProductoEmpresa"
	   add constraint  FK_Estado_CajaProductoEmpresa foreign key ("IdEstado")
		  references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.HorarioBancosPais"
	   add constraint  FK_Pais_HorarioBancosPais foreign key ("IdPais")
		      references "BSCL.Plataforma.BD::REG.Pais" ("IdPais") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.HorarioBancosPais"
	   add constraint  FK_Estado_HorarioBancosPais foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Empresa_CuentaBanco foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Banco_CuentaBanco foreign key ("IdBanco")
		      references "BSCL.Plataforma.BD::BAN.Banco" ("IdBanco") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_TipoCuentaBancaria_CuentaBanco foreign key ("IdTipoCuentaBancaria")
		      references "BSCL.Plataforma.BD::BAN.TipoCuentaBancaria" ("IdTipoCuentaBancaria") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Moneda_CuentaBanco foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.CuentaBanco"
	   add constraint  FK_Estado_CuentaBanco foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	   add constraint  FK_CuentaBanco_Chequera foreign key ("IdCuentaBanco")
		      references "BSCL.Tesoreria.BD::TES.CuentaBanco" ("IdCuentaBanco") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	   add constraint  FK_Estado_Chequera foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.EfectivoDenominacion"
	   add constraint  FK_Moneda_EfectivoDenominacion foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.EfectivoDenominacion"
	   add constraint  FK_Estado_EfectivoDenominacion foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Empresa_IngresoEgreso foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Modulo_IngresoEgreso foreign key ("IdModulo")
		      references "BSCL.Plataforma.BD::PLA.Modulo" ("IdModulo") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Usuario_IngresoEgreso foreign key ("IdUsuario")
		      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Concepto_IngresoEgreso foreign key ("IdConcepto")
		      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Moneda_IngresoEgreso foreign key ("IdMoneda")
		      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoDocumentoIdentidad_IngresoEgreso foreign key ("IdTipoDocumentoIdentidad")
		      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Caja_IngresoEgreso foreign key ("IdCaja")
		      references "BSCL.Tesoreria.BD::TES.Caja" ("IdCaja") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_Estado_IngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgreso"
	   add constraint  FK_TipoSocioDeNegocio_IngresoEgreso foreign key ("IdTipoPersona")
		      references "BSCL.SocioNegocio.BD::SOC.TipoSocioDeNegocio" ("IdTipoSocioDeNegocio") on delete restrict on update restrict;



/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_MedioDePago_IngresoEgresoFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_Estado_IngresoEgresoFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.IngresoEgresoFormaDePago"
	   add constraint  FK_IngresoEgreso_IngresoEgresoFormaDePago foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;


/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Empresa_Comprobante foreign key ("IdEmpresa")
		      references "BSCL.Plataforma.BD::PLA.Empresa" ("IdEmpresa") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Usuario_Comprobante foreign key ("IdUsuario")
	      references "BSCL.Plataforma.BD::ACC.Usuario" ("IdUsuario") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Concepto_Comprobante foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Moneda_Comprobante foreign key ("IdMoneda")
	      references "BSCL.Plataforma.BD::PLA.Moneda" ("IdMoneda") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante" 
	   add constraint  FK_Persona_Comprobante foreign key ("IdPersona")
	      references "BSCL.SocioNegocio.BD::PER.Persona" ("IdPersona") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_TipoDocumentoIdentidad_Comprobante foreign key ("IdTipoDocumentoIdentidad")
	      references "BSCL.Plataforma.BD::REG.TipoDocumentoIdentidad" ("IdTipoDocumentoIdentidad") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.Comprobante"
	   add constraint  FK_Estado_Comprobante foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;


/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Comprobante_ComprobanteFormaDePago foreign key ("IdComprobante")
		      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_MedioDePago_ComprobanteFormaDePago foreign key ("IdMedioDePago")
		      references "BSCL.Plataforma.BD::PAG.MedioDePago" ("IdMedioDePago") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteFormaDePago"
	   add constraint  FK_Estado_ComprobanteFormaDePago foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	/*alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Comprobante_ComprobanteDetalle foreign key ("IdComprobante")
		      references "BSCL.Tesoreria.BD::TES.Comprobante" ("IdComprobante") on delete restrict on update restrict;
    */
    
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalle foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	/*alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso"
	   add constraint  FK_ComprobanteDetalle_ComprobanteDetalleIngresoEgreso foreign key ("IdComprobanteDetalle")
		      references "BSCL.Tesoreria.BD::TES.ComprobanteDetalle" ("IdComprobanteDetalle") on delete restrict on update restrict;
   */
   
	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalleIngresoEgreso"
	   add constraint  FK_IngresoEgreso_ComprobanteDetalleIngresoEgreso foreign key ("IdIngresoEgreso")
		      references "BSCL.Tesoreria.BD::TES.IngresoEgreso" ("IdIngresoEgreso") on delete restrict on update restrict;

	alter table "BSCL.Tesoreria.BD::TES.ComprobanteDetalle"
	   add constraint  FK_Estado_ComprobanteDetalleIngresoEgreso foreign key ("IdEstado")
		      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.ConceptoTesoreria"
	   add constraint "FK_Concepto_ConceptoTesoreria" foreign key ("IdConcepto")
	      references "BSCL.Plataforma.BD::PLA.Concepto" ("IdConcepto") on delete restrict on update restrict;

/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.EstadoTesoreria"
	   add constraint "FK_Estado_EstadoTesoreria" foreign key ("IdEstado")
	      references "BSCL.Plataforma.BD::PLA.Estado" ("IdEstado") on delete restrict on update restrict;
	      
	     
/* Table: */
	alter table "BSCL.Tesoreria.BD::TES.Chequera"
	add constraint "FK_ConfiguracionChequera_Chequera" foreign key ("IdConfiguracionChequera")
	references "BSCL.Tesoreria.BD::TES.ConfiguracionChequera" ("IdConfiguracionChequera") on delete restrict on update restrict;

