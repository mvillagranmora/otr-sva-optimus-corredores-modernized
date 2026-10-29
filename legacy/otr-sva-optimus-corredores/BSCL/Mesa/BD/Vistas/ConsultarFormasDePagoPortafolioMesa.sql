SET SCHEMA "BSCL"; 
--IdServicio: 1785
GO
DROP VIEW "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa";
GO
CREATE VIEW "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa"
AS 
SELECT
	 pagMes."IdFormaDePago"
	,pagMes."Nombre" as "NombreFormaDePago"
	,pagMes."Dias"
	,pagPor."IdPreferenciaPagoPortafolio"
	,pagPor."IdPortafolio"
	,pagPor."IdPreferenciaPago"
	,pagPor."IdCuentaBancaria"
	,pagPor."IdBancoCuentaBancaria"
	,pagPor."NombreBancoCuentaBancaria"
	,pagPor."NumeroCuentaBancaria"
	,pagPor."IdTipoCuentaBancaria"
	,pagPor."NombreTipoCuentaBancaria"
	,pagPor."IdTarjetaBancaria"
	,pagPor."IdBancoTarjetaBancaria"
	,pagPor."NombreBancoTarjetaBancaria"
	,pagPor."NumeroTarjetaBancaria"
	,pagPor."IdTipoTarjetaBancaria"
	,pagPor."NombreTipoTarjetaBancaria"
	,pagPor."FechaCreacion"
	,pagPor."FechaModificacion"
	,pagPor."IdUsuarioCreador"
	,pagPor."IdEstado"
	,pagPor."Estado"
	,pagPor."IdEmpresa"
	,pagPor."IdTipoOperacion"
	,pagPor."TipoOperacion"
	,pagPor."IdMedioDePago"
	,pagPor."MedioDePago"
	,pagPor."Descripcion"
	,pagPor."IdMoneda"
	,pagPor."UsaPersonaRelacionada"
	,pagPor."TextoUsaPersonaRelacionada"
	,pagPor."IdPersonaRelacionada"
	,pagPor."NombrePersonaRelacionada"
	,pagPor."DocumentoIdentidadPersonaRelacionada"
	,pagPor."TextoPersonaRelacionada"
	,pagPor."UsaCuentaBancaria"
	,pagPor."TextoUsaCuentaBancaria"
	,pagPor."UsaTarjetaBancaria"
	,pagPor."TextoUsaTarjetaBancaria"
	,pagPor."RecibePagaPortafolio"
	,pagPor."TextoRecibePagaPortafolio"
	,pagPor."TextoPorDefecto"
	,pagPor."TextoPreferenciaPago"
	,pagPor."IdBancoTarjetaBancariaBancaria"
	,pagPor."IdTipoProducto"
	,pagPor."IdCliente"
	,pagPor."IdMatrizAgrupadorAPV"
	,CASE WHEN pagPor."PorDefecto" = 'S' AND pagMes."PorDefecto" = 'S' THEN 'S' ELSE 'N' END AS "PorDefecto"
	,pagPor."PorDefecto" AS "PorDefectoPortafolio"
    ,pagMes."PorDefecto" AS "PorDefectoMesa"
FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa" pagMes
INNER JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" pagPor ON (pagMes."IdPreferenciaPago" = pagPor."IdPreferenciaPago")
WHERE pagMes."IdEstado" = 'ACT'
  AND pagPor."IdEstado" = 'ACT'
;
GO
GRANT SELECT ON "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa" TO BCS_DESARROLLADOR WITH GRANT OPTION; 


/*
select * from "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa";
SELECT * FROM "BSCL.Portafolio.BD.Vistas::ConsultarPreferenciaPagoPortafolio" pagPor;
SELECT * FROM "BSCL.Mesa.BD::MES.FormaDePagoMesa";
SELECT * FROM "BSCL.Mesa.BD.Vistas::ConsultarFormasDePagoPortafolioMesa";

insert into  "BSCL.Mesa.BD::MES.FormaDePagoMesa" values ('COMTER_CHE_CLP_24','COMTER_CHE_CLP','CHEQUE 24',1,'2016-01-03','2016-01-03',9,'ACT',1);
insert into  "BSCL.Mesa.BD::MES.FormaDePagoMesa" values ('VENTER_EFE_CLP_24','VENTER_EFE_CLP','EFECTIVO 24',1,'2016-01-03','2016-01-03',9,'ACT',1);
insert into  "BSCL.Mesa.BD::MES.FormaDePagoMesa" values ('COMTER_EFE_CLP_24','COMTER_EFE_CLP','EFECTIVO 24',1,'2016-01-03','2016-01-03',9,'ACT',1);
insert into  "BSCL.Mesa.BD::MES.FormaDePagoMesa" values ('COMTER_CHE_CLP_48','COMTER_CHE_CLP','CHEQUE 48',2,'2016-01-03','2016-01-03',9,'ACT',1);
insert into  "BSCL.Mesa.BD::MES.FormaDePagoMesa" values ('COMTER_TRA_CLP_48','COMTER_TRA_CLP','TRANSFERENCIA 48',2,'2016-01-03','2016-01-03',9,'ACT',1);

select * from "BSCL.Mesa.BD::MES.FormaDePagoMesa"
*/
