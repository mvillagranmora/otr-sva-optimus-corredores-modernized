select * from "2022_05_31_TBCTMVTO" t where FEC_MVTO >= '2022-05-31'

select distinct COD_MOVTO  from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'


select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO >= '2022-05-31'

select RUT_CLI ,SEC_RUT_CLI  ,count(1) 
from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
group by RUT_CLI ,SEC_RUT_CLI 

select RUT_CLI ,SEC_RUT_CLI  ,count(1) 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
group by COD_MOVTO 

--cirv
select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('CFIC','CIIF','CIRV','CIRF','VIRF','VIRV','VIIF','FCRV',
'FCEX','FCIF','FCIU','FCOT','FCRF','FVIF','FVIU','FVRF','FVRV','FVTP',
'VDIF','VFIN','VITP','VRIC','VRRC')
and (abs(sebra.c1 - optimus.c1) > 100 or abs(sebra.c2 - optimus.c2) > 100 or optimus.c1 is null)
order by sebra.cod_movto

select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('ADCR','ARVD','ARVR','AVFR','CCCR','CDCR','CRCD','CRIC','CRIN','CRRC','RCIC','RCRC',
'RVIC','RVIN','RVRC')
--VTIF VCIF NO SE CARGAN, VTRF NO HABIA NINGUNO
and (abs(sebra.c1 - optimus.c1) > 100 or abs(sebra.c2 - optimus.c2) > 100 or optimus.c1 is null)
order by sebra.cod_movto


select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('DIPE','IGRV','NTPV','NVTP','PEDI','RGRV')
--VTIF VCIF NO SE CARGAN, VTRF NO HABIA NINGUNO
and (abs(sebra.c1 - optimus.c1) > 100 or abs(sebra.c2 - optimus.c2) > 100 or optimus.c1 is null)
order by sebra.cod_movto

select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('IIIF','IIRF','RIIF','RIRF')
--VTIF VCIF NO SE CARGAN, VTRF NO HABIA NINGUNO
and (abs(sebra.c1 - optimus.c1) > 100 or abs(sebra.c2 - optimus.c2) > 100 or optimus.c1 is null)
order by sebra.cod_movto

select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('ABDO','ABMN','CADO','CAMN')
--VTIF VCIF NO SE CARGAN, VTRF NO HABIA NINGUNO
and (abs(sebra.c2 - optimus.c2) > 100 or optimus.c1 is null)
order by sebra.cod_movto

--FVTP YA NO APARECEN EN OPTIMUS, LA FACTURA TP NO MUEVE CUENTA INVERSION, SOLO EL VENCIMIENTO
--descuadres en VIRV son igual que mvci, retiros mal cargados
--vtif y vcif no cuadraran ya que los vencimientos se cargaron como saldo de custodia o de dinero

-- CRIC 96678790-2 pacto que sobra en optimus
-- 97030000-7	0	VRIC falta un pacto de bci
-- 96564330-3	1	VRIC no cuadra la cantidad
--CRCD Y CRCR  no cuadran, los vencimientos del dia se cargan como abono inicial
--CRIC descuadres por movimientos de cartera
-- 60805000-0 CRIN aparecen como CRIC el pacto fue con custodia
-- 81585900-6 sobra un pacto en optimus
-- RGRV se cargaron como saldo para vencimiento de simultaneas
-- NVTP RVIC RVIN RVRC VTRF RCIC RCRC los vencimientos del dia se cargan como abono inicial
-- MOV MANUALES NO SE CUADRARAN RIIF RIRF IIIF IIRF

select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(monto) as c1
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('CFIF','CFIC','CIRV','VIRV','CIRF','VIRF','FCRV',
'FCEX','FCIF','FCIU','FCOT','FCRF','FVIF','FVIU','FVRF','FVRV','FVTP',
'VDIF','VFIN','VIIF','VITP','VRIC','VRRC','ADCR','AVFR','CCCR','CRIC',
'CRIN','CRRC','IGRV','NTPV') and (abs(c1 - c2) > 10 or c2 is null)  
order by sebra.cod_movto

select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(monto) as c1
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('ABDO','ABMN','CADO','CAMN') and (abs(c1 - c2) > 10 or c2 is null)

--PEDI Y DIPE CUADRADO POR MONTO, CANTIDAD NO LLEGA AL MODULO
select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('DIPE','PEDI') and (abs(sebra.c2 - optimus.c2) > 10 or optimus.c2 is null)

--TODO CUADRADO POR MONTO, CANTIDAD NO ESTA EN EL REGISTRO, ARVD falta un movimiento
select *
from 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "2022_05_31_TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as sebra
LEFT join 
(
	select RUT_CLI ,SEC_RUT_CLI  ,cod_movto,sum(cantidad) as c1,sum(monto) as c2
	from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
	and t.IND_ANULADO != 'A'
	group by RUT_CLI ,SEC_RUT_CLI  ,cod_movto
) as optimus on sebra.rut_cli = optimus.rut_cli and optimus.sec_rut_cli = sebra.sec_rut_cli and optimus.cod_movto = sebra.cod_movto
where sebra.cod_movto IN ('ARVR','ARVD','CRCD','CRCR','CDCR','ADCR') and (abs(sebra.c2 - optimus.c2) >1 or optimus.c2 is null)


--EN ALGUNOS CLIENTES SEBRA SALE CAMN VS DIPE + PEDI EN OPTIMUS
--PREGUNTAR ARTURO 480 CASOS
--TAMBIEN SALE EL VENCIMIENTO DEL PACTO COMO CAMN EN VEZ DE ARVR
 
 97030000-7	0	ABDO diferencias por operatoria de spot
 12539650-K	0	ABMN
 97030000-7	0	CADO diferencias por operatoria de spot
 91806000-6	0	CAMN venc
 99586280-8	0	CAMN venc
 81537600-5	0	CAMN venc
 96727830-0	0	CAMN venc
 76010229-6	0	CAMN venc
 76012676-4	0	CAMN venc
 96427000-7	0	CAMN venc
 96836390-5	85	CAMN movimiento invalido en sebra
 96678790-2	0	CAMN venc
 76090492-9	0	CAMN venc
 96856780-2	0	CAMN venc
 76106658-7	0	CAMN venc
 94141000-6	0	CAMN venc
 70016330-K	0	CAMN venc
 91705000-7	0	CAMN venc
 60805000-0	0	CAMN venc
 76099692-0	0	CAMN venc
 76362176-6	0	CAMN venc
 76838490-8	0	CAMN venc
 76840089-K	0	CAMN venc
 76883240-4	0	CAMN venc
 77032259-6	0	CAMN venc
 78996910-8	0	CAMN venc
 92434000-2	0	CAMN venc
 96854790-9	0	CAMN venc
 97030000-7	0	CAMN venc
 99564360-K	0	CAMN venc
 99589100-K	0	CAMN venc
 96945440-8	0	CAMN venc
 93458000-1	0	CAMN venc
 91041000-8	0	CAMN venc
 91004000-6	0	CAMN venc
 
select RUT_CLI ,VAL_RESC ,EMISOR ,FEC_VCTO ,TIPO_REAJ ,nemo,MONTO ,CANTIDAD ,MONTO_APLAZO ,COD_MOVTO ,FOLIO_COMP_ADJ ,* 
from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" t where FEC_MVTO = '2022-05-31'
and RUT_CLI = ' 12539650-K' and COD_MOVTO  = 'ABMN'


 97030000-7 mas movimientos
 94141000-6 nominal
 96678790-2 cartera extra en pacto
 81537600-5 mas movimientos
 81585900-6 cartera extra en pacto
 
select RUT_CLI ,VAL_RESC ,EMISOR ,FEC_VCTO ,TIPO_REAJ ,nemo,MONTO ,CANTIDAD ,MONTO_APLAZO ,COD_MOVTO ,FOLIO_COMP_ADJ ,* 
from "2022_05_31_TBCTMVTO" t2 where FEC_MVTO = '2022-05-31'
and RUT_CLI = ' 12539650-K' and COD_MOVTO  = 'ABMN'



590184.08
590184.08
9716.52
9716.52

PACTO VENTA
VRIC POR CADA PAPEL QUE SALE DE LA CORREDORA
CRIC INGRESO CUSTODIA PARA EL CLIENTE
CDCR CARGO PARA EL CLIENTE
ABDO PARA LA CORREDORA

PACTO COMPRA
CRIC POR CADA PAPEL QUE ENTRA A LA CORREDORA
VRIC RETIRO CUSTODIA PARA EL CLIENTE
ADCR ABONO PARA EL CLIENTE
CADO PARA LA CORREDORA

VENC PACTO VENTA CON RETROCOMPRA
ARVD PARA EL CLIENTE POR ABONO
RCIC PARA CORREDORA POR INGRESO DE TITULO
RVIC PARA CLIENTE POR RETIRO DE TITULO

VENC PACTO compra CON RETROventa
ARVR PARA EL CLIENTE POR CARGO
RCIC PARA CORREDORA POR RETIRO DE TITULO
RVIC PARA CLIENTE POR INGRESO DE TITULO


AVCR PESO
ADCR DOLAR
 
VTIF VENCIMIENTO IF CUSTODIA
VCIF VENCIMIENTO IF CUENTA INVERSION
VFIN VENTA NOMINAL A TERMINO CUSTODIA
AVFR Abono Venta FR IF-RF CUENTA INVERSION
VRIC Venta IF Retroc Custodia custodia
ARVR Abono Retrov IF-RF
CRCR Cargo Retroc IF-RF
ARVD
ARVR
CRRC Compra RF Retrov Custodia
RVIC re
RVIN
RVRC
VTRF

select IND_CTCTCL ,IND_CTCIRF ,IND_CTCIIF , * from "BSCL.Sebra.BD::MIG.TBPLTMOV" bsbmt where TIPO_REG = 'CM' and CODIGO in ('RCIC','RCRC','RVIC')


select * from "BSCL.Replica.BD.Vistas::Sebra.TBCTMVTO" brbvst  where SEC_MOVTO  = -20020


	WITH
	v_ffmm as (
		SELECT FM."IdEmpresa",'N' AS "EsIngresoManual",'FMM' AS "IdModulo",'FMM' AS "Origen",FM."IdSolicitudAporteRescate" AS "Folio",FM."FechaEmision" AS "FechaIngresoRegistro",FM."FechaEmision" AS "FechaOperacion",
				FM."FechaLiquidacion" AS "FechaVencimientoOperacion",FM."FechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				'' AS "IdCondicionLiquidacion",FM."Estado" AS "IdEstado",FM."IdCliente",FM."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				FM."TipoSolicitud" AS "IdTipoOperacion",'INFM' AS "IdTipoProducto",FM."IdTipoMovimientoFondoMutuo" as "IdConcepto",'FMM' AS "IdTipoComprobante",FM."IdSolicitudAporteRescate" AS "IdComprobante",
				FM."IdSolicitudAporteRescate" AS "FolioComprobante",0 AS "IdMesa",
				'FM' AS "IdClasificacionTipoInstrumento",'FFM' AS "IdTipoInstrumento",SER."IdInstrumento" AS "IdInstrumento",FONDO."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",FM."IdMoneda" AS "IdMonedaEmision",FM."IdMoneda" AS "IdMonedaOperacion",
				FM."IdSerieFondo" AS "Nemotecnico",'' AS "IdBolsa",'COR' AS "IdBoveda",'' AS "EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal",
			   	"NumeroCuotas" AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	FM."ValorCuota" AS "PrecioCompra",0 AS"TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	FM."Monto" AS "MontoCompra",FM."ValorCuota" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	FM."IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  FM."TipoSolicitud"= 'APO' THEN 'E' ELSE 'I' END   AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
		FROM "BSCL.FondoMutuo.BD::FMM.SolicitudAporteRescate" FM
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.SerieDeFondo" AS SER ON SER."IdSerieFondo" = FM."IdSerieFondo"
		LEFT JOIN "BSCL.FondoMutuo.BD::FMM.Fondo" AS FONDO ON FONDO."IdFondo" = SER."IdFondo" 
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON FM."IdPortafolio" = POR."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = FM."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		
		
	),
	v_nominales as (
		
	SELECT  transito."FolioExternoReferencia" ,TRA."Nemotecnico" , TRANSITO."Nemotecnico" , TRA."Precio" , tra."Tasa" ,TRANSITO."PrecioCompra" , TRA."IdTipoOperacion" , TRANSITO."IdTipoOperacion", TRA.*
		FROM "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = TRANSITO."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdOperacion"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = TRANSITO."FolioExternoReferencia" AND TRA."IdTipoOperacion" = TRANSITO."IdTipoOperacion" and (TRA."FechaSistemaCreacion" = TRANSITO."FechaOperacion" OR (TRA."Nemotecnico" = TRANSITO."Nemotecnico" AND TRA."Precio" = TRANSITO."PrecioCompra" and tra."Tasa"  = transito."TasaCompra"  ))
		WHERE (TRANSITO."IdTipoOperacion" IN ('COMACN','COMCFI','COMCFX','COMETN','COMETX','VENACN','VENCFI','VENETN','VENETX','VENCFX','COMRF','COMIIF','VENRF','VENIIF','RETTIT','INGTIT','COMTER','VENTER')
		and (TRANSITO."EsNominal" = 'S') OR (TRANSITO."EsNominal" = 'N' and TRANSITO."IdEstado" = 'PEN'))
		and "IdMovimientoCarteraEnTransito"  = 1932
		
	),
	v_cartera_ingreso as (
	
		SELECT
				
				CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ICU' AS "Origen",CUS."IdCartera" AS "Folio",CUS."FechaIngresoRegistro",CUS."FechaOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."FechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."IdCondicionLiquidacion",CUS."IdEstado" AS "IdEstado",CUS."IdCliente",CUS."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CUS."INGIdTipoOperacion" AS "IdTipoOperacion",CUS."IdTipoProducto" AS "IdTipoProducto",CUS."IdConcepto",CUS."IdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."FolioComprobante",0) WHEN 0 THEN 0 ELSE CUS."FolioComprobante" END as "FolioComprobante",CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CASE 	
					WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') AND TRA."NemotecnicoSubyacente" IS NOT NULL THEN TRA."NemotecnicoSubyacente"
					WHEN CUS."IdTipoOperacion" IN ('PRESTA') THEN SUB."Nemotecnico"
					ELSE CUS."Nemotecnico" END AS "Nemotecnico",
				CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."Cantidad",CUS."ValorRescate",CUS."IdTipoReajuste",
				CASE WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') THEN CUS."PrecioAPlazo" ELSE CUS."PrecioCompra" END AS "PrecioCompra",CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	CASE WHEN CUS."Nemotecnico" IN ('SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA') THEN CUS."MontoAPlazo" ELSE CUS."MontoCompra" END AS "MontoCompra",
			   	
			   	CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",
			   		CASE CUS."IdTipoComprobante" WHEN 'FAE' THEN CUS."FolioExternoReferencia" ELSE '' END AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal", CASE WHEN CUS."IdConcepto" IN ('VNSIMU','CMSIMU', 'CMACEX', 'CMACMI', 'CMACNA', 'CMCFEX', 'CMCFIN', 'CMCFMN', 'CMDVFU', 'CMETFN', 'CMETFX', 'CMINFI', 'CMINRF', 'CMMONE', 'CMOSAC', 'COMIFI', 'COMIRF', 'COMIRV') THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona"

				
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."IdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."FolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."FechaOperacion"
			AND TRA."IdTipoOperacion" = CUS."IdTipoOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CUS."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON SUB."FolioCarteraRelacionada" = CUS."FolioCartera" AND SUB."EsPrimeraCartera" = 'S'
		
		WHERE CUS."IdCarga" = 0 and CUS."EsPrimeraCartera" = 'S' AND TIPINS."IdClasificacionTipoInstrumento" != 'FM' and TIPINS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA') and CUS."IdTipoOperacion" NOT IN ('EMPRES','VENEMP')
		and CUS."ValorRescate" <= 999999999999999
	),
	v_cartera_retiro as (
	
		SELECT CUS."IdEmpresa",'N' AS "EsIngresoManual",'CUS' AS "IdModulo",'ECU' AS "Origen",-1*CUS."IdCartera" AS "Folio",CUS."FechaIngresoRegistro",CUS."EGRFechaOperacion" AS "FechaOperacion",
				--CUS."EGRFechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN CUS."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN CUS."FechaVencimientoInstrumento" <= '1900-01-01' THEN CUS."FechaVencimientoOperacion" ELSE CUS."FechaVencimientoInstrumento" END
					ELSE CUS."FechaVencimientoOperacion"
				END	AS "FechaVencimientoOperacion",
				CUS."EGRFechaLiquidacion" AS "FechaLiquidacion",CUS."PlazoEnDiasOperacion" as "Plazo",
				CUS."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",CUS."IdEstado" AS "IdEstado",CUS."IdCliente",CUS."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CUS."EGRIdTipoOperacion" AS "IdTipoOperacion",CUS."IdTipoProducto" AS "IdTipoProducto",CUS."EGRIdConcepto" as "IdConcepto",CUS."EGRIdTipoComprobante" AS "IdTipoComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE "EGRFolioComprobante" END AS "IdComprobante",
				CASE COALESCE(CUS."EGRFolioComprobante",0) WHEN 0 THEN 0 ELSE "EGRFolioComprobante" END as "FolioComprobante",CUS."IdMesa" AS "IdMesa",
				CUS."IdClasificacionTipoInstrumento",CUS."IdTipoInstrumento",CUS."IdInstrumento",CUS."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",CUS."IdMonedaEmision",CUS."IdMonedaInstrumento" AS "IdMonedaOperacion",
				CUS."Nemotecnico",CUS."IdBolsa" AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	CUS."EGRCantidad" as "Cantidad",CUS."EGRValorRescate" as "ValorRescate",CUS."IdTipoReajuste",
			   	CUS."EGRPrecio" AS "PrecioCompra",CUS."TasaCompra" AS "TasaCompra",CUS."TasaAgente" AS "TasaAgente",0 AS "TasaEmision",
			   	CUS."EGRMontoMonedaInstrumento" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",CUS."CodigoIFRS" AS "CodigoIFRS",CUS."EGRFolioExternoReferencia" AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CASE WHEN CUS."EGRIdConcepto" IN ('VENIFI', 'VENIRF', 'VENIRV', 'VNACEX', 'VNACMI', 'VNACNA', 'VNCFEX', 'VNCFIN', 'VNCFMN', 'VNDVFU', 'VNETFN', 'VNETFX', 'VNINFI', 'VNINRF', 'VNMONE', 'VNOSAC', 'VNSIMU')  THEN ORDEN."IdOrden" ELSE 0 END AS "IdOrden",
			   	COALESCE(CORR."CodigoCorredora",0) AS "CodigoCorredorContraparte",CLI."IdPersona"
		FROM "BSCL.Custodia.BD::CUS.Cartera" CUS
		LEFT JOIN "BSCL.Custodia.BD::CUS.MovimientoCarteraEnTransito" TRANSITO ON TRANSITO."IdMovimientoCarteraEnTransito" = CUS."EGRIdMovimientoCarteraEnTransito"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = CUS."IdPortafolio"
		LEFT JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVTOFAC ON MOVTOFAC."IdMovimientoComprobante" = TRANSITO."IdMovimientoComprobante"
		LEFT JOIN "BSCL.Orden.BD::ORD.Orden" ORDEN ON ORDEN."IdOrden" = MOVTOFAC."IdOrden"
		LEFT JOIN "BSCL.Orden.BD::ORD.Transaccion" TRA ON TRA."Folio" = CUS."EGRFolioExternoReferencia" AND TRA."FechaSistemaCreacion" = CUS."EGRFechaOperacion"
		LEFT JOIN "BSCL.Plataforma.BD::INS.CorredoraCodigos" CORR ON TRA."IdCorredorContraparte" = CORR."IdCorredora" and CORR."IdBolsa" = TRA."IdBolsa"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on CUS."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = CUS."IdCliente"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE TIPINS."IdClasificacionTipoInstrumento" != 'FM' and TIPINS."IdTipoInstrumento" != 'FWD'
		AND CUS."Nemotecnico" NOT IN ('PACTO VENTA','PACTO COMPRA','SIMULTÁNEA VENTA','SIMULTÁNEA COMPRA')
		and CUS."EGRCantidad" > 0 and CUS."IdConcepto" NOT IN('CMSIMU')
		and CUS."EGRValorRescate" <= 999999999999999
		and cus."IdCartera" = 20020
		
	),
	v_garantia_ingreso as (
	
		SELECT * FROM (SELECT rank() OVER (PARTITION BY "FolioGarantia" ORDER BY "IdGarantia" asc) AS RANKING,
				
				
				GTA."IdEmpresa",'N' AS "EsIngresoManual",'GTA' AS "IdModulo",'IGT' AS "Origen",GTA."IdGarantia" AS "Folio",GTA."FechaIngresoRegistro",GTA."FechaOperacion",
				--GTA."FechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN GTA."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN GTA."FechaVencimientoInstrumento" <= '1900-01-01' THEN GTA."FechaVencimientoOperacion" ELSE GTA."FechaVencimientoInstrumento" END
					ELSE 
						CASE WHEN GTA."FechaVencimientoOperacion" <= '1900-01-01' THEN GTA."FechaLiquidacion" ELSE GTA."FechaVencimientoOperacion" END
				END	AS "FechaVencimientoOperacion",
				GTA."FechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				GTA."IdCondicionLiquidacion",GTA."IdEstado" AS "IdEstado",GTA."IdCliente",GTA."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				'INGGTA' AS "IdTipoOperacion",'' AS "IdTipoProducto",GTA."IdConcepto",'CUS' AS "IdTipoComprobante",GTA."FolioComprobante" AS "IdComprobante",
				GTA."FolioComprobante" as "FolioComprobante",0 AS "IdMesa",
				GTA."IdClasificacionTipoInstrumento",GTA."IdTipoInstrumento",GTA."IdInstrumento",GTA."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",GTA."IdMonedaEmision",GTA."IdMonedaInstrumento" AS "IdMonedaOperacion",
				GTA."Nemotecnico",'' AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	GTA."Cantidad",GTA."ValorRescate",CUS."IdTipoReajuste",
			   	GTA."PrecioGarantia" AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	GTA."MontoGarantia" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
				
		FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CUS on CUS."IdCartera" = GTA."IdCarteraOrigen"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on GTA."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = GTA."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = GTA."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = por."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE GTA."IdCarga" = 0 and GTA."IdOrigenGarantia" ='CUS'
		) WHERE RANKING = 1
		
	),
	v_garantia_retiro as (
	
		SELECT	GTA."IdEmpresa",'N' AS "EsIngresoManual",'GTA' AS "IdModulo",'RGT' AS "Origen", -1*GTA."IdGarantia" AS "Folio",GTA."FechaIngresoRegistro",GTA."EGRFechaOperacion" AS "FechaOperacion",
				--GTA."EGRFechaLiquidacion" AS "FechaVencimientoOperacion",
				CASE 
					WHEN GTA."IdClasificacionTipoInstrumento" IN ('RF','IF') THEN 
						CASE WHEN GTA."FechaVencimientoInstrumento" <= '1900-01-01' THEN GTA."FechaVencimientoOperacion" ELSE GTA."FechaVencimientoInstrumento" END
					ELSE 
						CASE WHEN GTA."FechaVencimientoOperacion" <= '1900-01-01' THEN GTA."EGRFechaLiquidacion" ELSE GTA."FechaVencimientoOperacion" END
				END	AS "FechaVencimientoOperacion",
				GTA."EGRFechaLiquidacion" AS "FechaLiquidacion",0 as "Plazo",
				GTA."EGRIdCondicionLiquidacion" AS "IdCondicionLiquidacion",GTA."IdEstado" AS "IdEstado",GTA."IdCliente",GTA."IdPortafolio",0 as "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				'RETGTA' AS "IdTipoOperacion",'' AS "IdTipoProducto",GTA."EGRIdConcepto" AS "IdConcepto",'CUS' AS "IdTipoComprobante",GTA."EGRFolioComprobante" AS "IdComprobante",
				GTA."EGRFolioComprobante" as "FolioComprobante",0 AS "IdMesa",
				GTA."IdClasificacionTipoInstrumento",GTA."IdTipoInstrumento",GTA."IdInstrumento",GTA."IdEmisor",
				'0001-01-01' as "FechaEmisionInstrumento",GTA."IdMonedaEmision",GTA."IdMonedaInstrumento" AS "IdMonedaOperacion",
				GTA."Nemotecnico",'' AS "IdBolsa",CUS."IdBoveda",CUS."EsFungible",COALESCE(EMP."CodigoEmpleado",'') AS "CodigoAgente",COALESCE(SUC."CodigoSucursal",'') as "CodigoSucursal",
			   	GTA."EGRCantidad" AS "Cantidad",GTA."EGRValorRescate" as "ValorRescate",CUS."IdTipoReajuste",
			   	GTA."EGRPrecio" AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	GTA."EGRMontoMonedaInstrumento" AS "MontoCompra",CUS."INGPrecioMercadoActual" AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",PER."NombreCompleto" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	'' AS "IngresoEgreso",'N' AS "Nominal",CLI."IdPersona"
				
		FROM "BSCL.Custodia.BD::GTA.Garantia" GTA
		LEFT JOIN "BSCL.Custodia.BD::CUS.Cartera" CUS on CUS."IdCartera" = GTA."IdCarteraOrigen"
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on GTA."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = INS."IdTipoInstrumento"
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = GTA."IdCliente"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = GTA."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = por."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = EMP."IdSucursal"
		LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaNombreCompleto" PER ON CLI."IdPersona" = PER."IdPersona"
		WHERE GTA."IdCarga" = 0 AND GTA."EGRCantidad" > 0  and GTA."IdOrigenGarantia" ='CUS'
		
	),
	v_CuentaInversion as (
		SELECT 	MOV."IdEmpresa","EsIngresoManual","IdModulo",'CTA' AS "Origen",CASE WHEN "IdModulo" = 'TES' THEN MOV."IdComprobante" ELSE MOV."IdCargoAbonoMovimiento" END AS "Folio",MOV."FechaIngresoRegistro",MOV."FechaSistema" AS "FechaOperacion",
				MOV."FechaEfectivaMovimiento" AS "FechaVencimientoOperacion",MOV."FechaEfectivaMovimiento" AS "FechaLiquidacion",0 AS "Plazo",
				MOV."IdCondicionDeLiquidacion" AS "IdCondicionLiquidacion",MOV."IdEstado",CI."IdCliente" AS "IdCliente",CI."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				MOV."IdTipoOperacion",MOV."IdTipoProducto",MOV."IdConcepto",MOV."IdTipoComprobante",MOV."IdComprobante",
				CASE COALESCE(MOV."FolioComprobante",'') WHEN '' THEN '0' ELSE MOV."FolioComprobante" END AS "FolioComprobante",0 AS "IdMesa",
				TIPINS."IdClasificacionTipoInstrumento",TIPINS."IdTipoInstrumento",MOV."IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",CI."IdMoneda" AS "IdMonedaOperacion",
				
				CASE WHEN MOV."IdConcepto" IN ('VCTSIC','VCTSIV','ANTSIV','ANTSIC') THEN SUB."Nemotecnico" ELSE INS."Nemotecnico" END AS "Nemotecnico",
				
				'' AS "IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",EMP."CodigoEmpleado" AS "CodigoAgente",SUC."CodigoSucursal",
			   	--MOV."Cantidad",
			   	
			   	CASE WHEN MOV."IdTipoOperacion" IN ('COMSPT','VENSPT')  AND "IdModulo" = 'FAC'
					THEN COM."Cantidad" 
					ELSE MOV."Cantidad" 
					END AS "Cantidad",
			   	
			   	0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	MOV."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",MOV."NombreCliente" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  MOV."IdTipoMovimiento"= 'MOVABN' THEN 'I' ELSE 'E' END   AS "IngresoEgreso",'N' AS "Nominal",COALESCE(EVC."Secuencia",0) AS "SecuenciaEECC",CLI."IdPersona"
		FROM  "BSCL.CuentaInversion.BD::CTA.CargoAbonoMovimiento" AS MOV
		LEFT JOIN "BSCL.Plataforma.BD::INS.Instrumento" INS on MOV."IdInstrumento" = INS."IdInstrumento"
		LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento" TIPINS ON TIPINS."IdTipoInstrumento" = COALESCE(INS."IdTipoInstrumento",MOV."IdTipoInstrumento")
		LEFT JOIN "BSCL.CuentaInversion.BD::CTA.CuentaInversion" AS CI ON CI."IdCuentaInversion" = MOV."IdCuentaInversion"
		LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON por."IdPortafolio" = CI."IdPortafolio"
		LEFT JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" AS EMP on EMP."IdEmpleado" = POR."IdEmpleadoEjecutivo"
		LEFT JOIN "BSCL.Plataforma.BD::PLA.Sucursal" AS SUC on SUC."IdSucursal" = MOV."IdSucursal"
		LEFT JOIN "BSCL.Custodia.BD::CUS.EventoCapitalCliente" EVC ON EVC."IdEventoCapitalCliente" = MOV."IdOperacion" AND MOV."IdConcepto" = 'CIECDV'
		LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" CLI ON CLI."IdCliente" = POR."IdCliente"
		LEFT JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB ON CAST(SUB."FolioCarteraRelacionada" AS NVARCHAR) = MOV."FolioComprobante" AND MOV."IdConcepto" IN ('VCTSIC','VCTSIV','ANTSIV','ANTSIC') AND SUB."IdEstado" IN ('ANT','VCD')
		LEFT JOIN "BSCL.Facturacion.BD::FAC.Comprobante" COM ON MOV."IdComprobante" = COM."IdComprobante"
	),
	v_CmbtesTes as (
		SELECT 	CMBTE."IdEmpresa",'N' AS "EsIngresoManual",'TES' AS "IdModulo",'TES' AS "Origen","IdComprobante" AS "Folio",CMBTE."FechaIngresoRegistro",CMBTE."FechaMovimiento" AS "FechaOperacion",
				CMBTE."FechaLiquidacion" AS "FechaVencimientoOperacion",CMBTE."FechaLiquidacion" AS "FechaLiquidacion",0 AS "Plazo",
				'T0' AS "IdCondicionLiquidacion",CMBTE."IdEstado",0 AS "IdCliente",0 AS "IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				CMBTE."IdTipoMovimiento" AS "IdTipoOperacion",'' AS "IdTipoProducto",CMBTE."IdConcepto",'' AS "IdTipoComprobante",CMBTE."IdComprobante",
				"IdComprobante" AS "FolioComprobante",0 AS "IdMesa",
				'' AS "IdClasificacionTipoInstrumento",'' AS "IdTipoInstrumento", 0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",CMBTE."IdMoneda" AS "IdMonedaOperacion",
				'' AS "Nemotecnico",'' AS "IdBolsa",'' AS "IdBoveda",'' AS "EsFungible",'' AS "CodigoAgente",'' AS "CodigoSucursal",
			   	0 AS "Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	0 AS "PrecioCompra",0 AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	CMBTE."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",CMBTE."NombrePersona" AS "EmitidoA",0 AS "Comision",0 AS "Gastos",
			   	CASE WHEN  CMBTE."IdTipoMovimiento"= 'MOVING' THEN 'I' ELSE 'E' END   AS "IngresoEgreso",'N' AS "Nominal",0 AS "SecuenciaEECC","IdPersona"
		FROM  "BSCL.Tesoreria.BD::TES.Comprobante" AS CMBTE
		WHERE CMBTE."IdTipoPersona" IN ('CNT','PER','PRO')
		
	),
	v_Facturacion as (
		SELECT 	"IdEmpresa",'' AS "EsIngresoManual",'FAC' AS"IdModulo",'MFAC' AS "Origen",COM."IdComprobante" AS "Folio",COM."FechaIngresoRegistro",'0001-01-01' AS "FechaOperacion",
				'0001-01-01' AS "FechaVencimientoOperacion",COM."FechaLiquidacion",COM."DiasPlazo" AS "Plazo",
				COM."IdCondicionLiquidacion",COM."IdEstadoComprobante" AS "IdEstado",COM."IdCliente",COM."IdPortafolio",0 AS "IdClienteCorredora",0 AS "IdPortafolioCorredora",
				COM."IdTipoOperacion",COM."IdTipoProducto",COM."IdConcepto" AS "IdConcepto",COM."IdTipoComprobante",COM."IdComprobante",CAST(COM."FolioComprobante" AS DECIMAL) AS "FolioComprobante",
				0 AS "IdMesa",COM."IdClasificacionTipoInstrumento",COM."IdTipoInstrumento",0 AS "IdInstrumento",'' AS "IdEmisor",
				'0001-01-01' AS "FechaEmisionInstrumento",'' AS "IdMonedaEmision",'' AS "IdMonedaOperacion",
				'' AS "Nemotecnico",COM."IdBolsa",COM."IdBoveda",'' AS "EsFungible",COM."IdEmpleadoEjecutivoPortafolio" AS "CodigoAgente",'' AS "CodigoSucursal",
			   	COM."Cantidad",0 AS "ValorRescate",'' AS "IdTipoReajuste",
			   	"Precio" AS "PrecioCompra","Tasa" AS "TasaCompra",0 AS "TasaAgente",0 AS "TasaEmision",
			   	COM."Monto" AS "MontoCompra",0 AS "PrecioMercado",'' AS "CodigoIFRS",'' AS "FolioTransaccion",
			   	'' AS "IdMedioDePago",'' AS "EmitidoA", CBS."Comision", CBS."Gastos",
			   	''  AS "IngresoEgreso"
		FROM "BSCL.Facturacion.BD::FAC.Comprobante" AS COM
		LEFT JOIN (
			SELECT "IdComprobante", cast(SUM("COFI"+"COSI"+"COVA"+"COAI"+"COIF"+"CCOR") as decimal(26,8)) AS "Comision",SUM("GFOD"+"GFFA"+"GAFI") AS "Gastos"
			FROM (
				SELECT  "IdComprobante",
					--COMISIONES
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COFI' THEN "MontoCobro" END),0) AS "COFI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COSI' THEN "MontoCobro" END),0) AS "COSI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COVA' THEN "MontoCobro" END),0) AS "COVA",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COAI' THEN "MontoCobro" END),0) AS "COAI",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'COIF' THEN "MontoCobro" END),0) AS "COIF",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'CCOR' THEN "MontoCobro" END),0) AS "CCOR",
					--GASTOS
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFOD' THEN "MontoCobro" END),0) AS "GFOD",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GFFA' THEN "MontoCobro" END),0) AS "GFFA",
					COALESCE(MAX(CASE WHEN "IdCobro" = 'GAFI' THEN "MontoCobro" END),0) AS "GAFI"
				FROM (
						SELECT "IdComprobante", "IdCobro", "MontoCobro"
						FROM "BSCL.Facturacion.BD::FAC.ComprobanteCobro"
				     )
				GROUP BY "IdComprobante"
				)
			GROUP BY "IdComprobante"
		) AS CBS ON CBS."IdComprobante" = COM."IdComprobante"	
	)
	
        SELECT
	        *
     FROM (
		SELECT  CTA."IdEmpresa",CTA."EsIngresoManual",CTA."IdModulo",CTA."Origen",CTA."Folio",CTA."FechaIngresoRegistro",CTA."FechaOperacion",CTA."FechaVencimientoOperacion",CTA."FechaLiquidacion",CTA."Plazo",
				CTA."IdCondicionLiquidacion",CTA."IdEstado",CTA."IdCliente",CTA."IdPortafolio",CTA."IdClienteCorredora",CTA."IdPortafolioCorredora",
				CTA."IdTipoOperacion",CTA."IdTipoProducto",CTA."IdConcepto",CTA."IdTipoComprobante",CTA."FolioComprobante",CTA."IdMesa",
				CTA."IdClasificacionTipoInstrumento",CTA."IdTipoInstrumento",CTA."IdInstrumento",CTA."FechaEmisionInstrumento",CTA."IdMonedaEmision",
				CTA."IdMonedaOperacion",CTA."IdEmisor",CTA."Nemotecnico",CTA."EsFungible",FAC."IdBolsa",CTA."IdBoveda",CTA."CodigoAgente",CTA."CodigoSucursal",
				CTA."IdTipoReajuste",CTA."Cantidad",CTA."ValorRescate",
				FAC."PrecioCompra",CTA."TasaCompra",CTA."TasaAgente",CTA."TasaEmision",CTA."MontoCompra",CTA."PrecioMercado",CTA."CodigoIFRS",
				CTA."FolioTransaccion",CTA."IdMedioDePago",CTA."EmitidoA",FAC."Comision",FAC."Gastos",CTA."IngresoEgreso",CTA."IdComprobante",CTA."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				CTA."SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",CTA."IdPersona"
		FROM v_CuentaInversion CTA
		LEFT JOIN v_Facturacion FAC ON CTA."IdComprobante" = FAC."IdComprobante" AND CTA."IdTipoComprobante"  IN ('FAE','FEE','NCE','NEE','FOX')
		UNION
		SELECT  FMM."IdEmpresa",FMM."EsIngresoManual",FMM."IdModulo",FMM."Origen",FMM."Folio",FMM."FechaIngresoRegistro",FMM."FechaOperacion",FMM."FechaVencimientoOperacion",FMM."FechaLiquidacion",FMM."Plazo",
				FMM."IdCondicionLiquidacion",FMM."IdEstado",FMM."IdCliente",FMM."IdPortafolio",FMM."IdClienteCorredora",FMM."IdPortafolioCorredora",
				FMM."IdTipoOperacion",FMM."IdTipoProducto",FMM."IdConcepto",FMM."IdTipoComprobante",FMM."FolioComprobante",FMM."IdMesa",
				FMM."IdClasificacionTipoInstrumento",FMM."IdTipoInstrumento",FMM."IdInstrumento",FMM."FechaEmisionInstrumento",FMM."IdMonedaEmision",
				FMM."IdMonedaOperacion",FMM."IdEmisor",FMM."Nemotecnico",FMM."EsFungible",FMM."IdBolsa",FMM."IdBoveda",FMM."CodigoAgente",FMM."CodigoSucursal",
				FMM."IdTipoReajuste",FMM."Cantidad",FMM."ValorRescate",
				FMM."PrecioCompra",FMM."TasaCompra",FMM."TasaAgente",FMM."TasaEmision",FMM."MontoCompra",FMM."PrecioMercado",FMM."CodigoIFRS",
				FMM."FolioTransaccion",FMM."IdMedioDePago",FMM."EmitidoA",FMM."Comision",FMM."Gastos",FMM."IngresoEgreso",FMM."IdComprobante",FMM."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",FMM."IdPersona"
		FROM v_ffmm FMM
		UNION
		SELECT  GTAING."IdEmpresa",GTAING."EsIngresoManual",GTAING."IdModulo",GTAING."Origen",GTAING."Folio",GTAING."FechaIngresoRegistro",GTAING."FechaOperacion",
				GTAING."FechaVencimientoOperacion",GTAING."FechaLiquidacion",GTAING."Plazo",
				GTAING."IdCondicionLiquidacion",GTAING."IdEstado",GTAING."IdCliente",GTAING."IdPortafolio",GTAING."IdClienteCorredora",GTAING."IdPortafolioCorredora",
				GTAING."IdTipoOperacion",GTAING."IdTipoProducto",GTAING."IdConcepto",GTAING."IdTipoComprobante",GTAING."FolioComprobante",GTAING."IdMesa",
				GTAING."IdClasificacionTipoInstrumento",GTAING."IdTipoInstrumento",GTAING."IdInstrumento",GTAING."FechaEmisionInstrumento",GTAING."IdMonedaEmision",
				GTAING."IdMonedaOperacion",GTAING."IdEmisor",GTAING."Nemotecnico",GTAING."EsFungible",GTAING."IdBolsa",GTAING."IdBoveda",GTAING."CodigoAgente",GTAING."CodigoSucursal",
				GTAING."IdTipoReajuste",GTAING."Cantidad",GTAING."ValorRescate",
				GTAING."PrecioCompra",GTAING."TasaCompra",GTAING."TasaAgente",GTAING."TasaEmision",GTAING."MontoCompra",GTAING."PrecioMercado","CodigoIFRS",
				GTAING."FolioTransaccion",GTAING."IdMedioDePago",GTAING."EmitidoA",GTAING."Comision",GTAING."Gastos",GTAING."IngresoEgreso",GTAING."IdComprobante",GTAING."Nominal",0 as "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",GTAING."IdPersona"
		FROM v_garantia_ingreso GTAING
		UNION
		SELECT  GTARET."IdEmpresa",GTARET."EsIngresoManual",GTARET."IdModulo",GTARET."Origen",GTARET."Folio",GTARET."FechaIngresoRegistro",GTARET."FechaOperacion",
				GTARET."FechaVencimientoOperacion",GTARET."FechaLiquidacion",GTARET."Plazo",
				GTARET."IdCondicionLiquidacion",GTARET."IdEstado",GTARET."IdCliente",GTARET."IdPortafolio",GTARET."IdClienteCorredora",GTARET."IdPortafolioCorredora",
				GTARET."IdTipoOperacion",GTARET."IdTipoProducto",GTARET."IdConcepto",GTARET."IdTipoComprobante",GTARET."FolioComprobante",GTARET."IdMesa",
				GTARET."IdClasificacionTipoInstrumento",GTARET."IdTipoInstrumento",GTARET."IdInstrumento",GTARET."FechaEmisionInstrumento",GTARET."IdMonedaEmision",
				GTARET."IdMonedaOperacion",GTARET."IdEmisor",GTARET."Nemotecnico",GTARET."EsFungible",GTARET."IdBolsa",GTARET."IdBoveda",GTARET."CodigoAgente",GTARET."CodigoSucursal",
				GTARET."IdTipoReajuste",GTARET."Cantidad",GTARET."ValorRescate",
				GTARET."PrecioCompra",GTARET."TasaCompra",GTARET."TasaAgente",GTARET."TasaEmision",GTARET."MontoCompra",GTARET."PrecioMercado","CodigoIFRS",
				GTARET."FolioTransaccion",GTARET."IdMedioDePago",GTARET."EmitidoA",GTARET."Comision",GTARET."Gastos",GTARET."IngresoEgreso",GTARET."IdComprobante",GTARET."Nominal",0 AS "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",GTARET."IdPersona"
		FROM v_garantia_retiro GTARET
		UNION
		SELECT  nom."IdEmpresa",nom."EsIngresoManual",nom."IdModulo",nom."Origen",nom."Folio",nom."FechaIngresoRegistro",nom."FechaOperacion",
				nom."FechaVencimientoOperacion",nom."FechaLiquidacion",nom."Plazo",
				nom."IdCondicionLiquidacion",nom."IdEstado",nom."IdCliente",nom."IdPortafolio",nom."IdClienteCorredora",nom."IdPortafolioCorredora",
				nom."IdTipoOperacion",nom."IdTipoProducto",nom."IdConcepto",nom."IdTipoComprobante",nom."FolioComprobante",nom."IdMesa",
				nom."IdClasificacionTipoInstrumento",nom."IdTipoInstrumento",nom."IdInstrumento",nom."FechaEmisionInstrumento",nom."IdMonedaEmision",
				nom."IdMonedaOperacion",nom."IdEmisor",nom."Nemotecnico",nom."EsFungible",nom."IdBolsa",nom."IdBoveda",nom."CodigoAgente",nom."CodigoSucursal",
				nom."IdTipoReajuste",nom."Cantidad",nom."ValorRescate",
				nom."PrecioCompra",nom."TasaCompra",nom."TasaAgente",nom."TasaEmision",nom."MontoCompra",nom."PrecioMercado",nom."CodigoIFRS",
				nom."FolioTransaccion",nom."IdMedioDePago",nom."EmitidoA",
				CASE WHEN nom."IdComprobante" IS NOT NULL THEN ROUND(FAC."Comision" * nom."Cantidad" / FAC."Cantidad",4 ) ELSE nom."Comision" END AS "Comision",
				CASE WHEN nom."IdComprobante" IS NOT NULL THEN ROUND(FAC."Gastos" * nom."Cantidad" / FAC."Cantidad",4 ) ELSE nom."Gastos" END AS "Gastos",
				nom."IngresoEgreso",nom."IdComprobante",nom."Nominal",nom."IdOrden",nom."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",nom."IdPersona"
		FROM v_nominales nom
		LEFT JOIN v_Facturacion FAC ON nom."IdComprobante" = FAC."IdComprobante" AND nom."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT  cing."IdEmpresa",cing."EsIngresoManual",cing."IdModulo",cing."Origen",cing."Folio",cing."FechaIngresoRegistro",cing."FechaOperacion",
				cing."FechaVencimientoOperacion",cing."FechaLiquidacion",cing."Plazo",
				cing."IdCondicionLiquidacion",cing."IdEstado",cing."IdCliente",cing."IdPortafolio",cing."IdClienteCorredora",cing."IdPortafolioCorredora",
				cing."IdTipoOperacion",cing."IdTipoProducto",cing."IdConcepto",cing."IdTipoComprobante",cing."FolioComprobante",cing."IdMesa",
				cing."IdClasificacionTipoInstrumento",cing."IdTipoInstrumento",cing."IdInstrumento",cing."FechaEmisionInstrumento",cing."IdMonedaEmision",
				cing."IdMonedaOperacion",cing."IdEmisor",cing."Nemotecnico",cing."EsFungible",cing."IdBolsa",cing."IdBoveda",cing."CodigoAgente",cing."CodigoSucursal",
				cing."IdTipoReajuste",cing."Cantidad",cing."ValorRescate",
				cing."PrecioCompra",cing."TasaCompra",cing."TasaAgente",cing."TasaEmision",cing."MontoCompra",cing."PrecioMercado",cing."CodigoIFRS",
				cing."FolioTransaccion",cing."IdMedioDePago",cing."EmitidoA",
				CASE WHEN cing."IdComprobante" IS NOT NULL THEN ROUND(FAC."Comision" * cing."Cantidad" / FAC."Cantidad",4 ) ELSE cing."Comision" END AS "Comision",
				CASE WHEN cing."IdComprobante" IS NOT NULL THEN ROUND(FAC."Gastos" * cing."Cantidad" / FAC."Cantidad",4 ) ELSE cing."Gastos" END AS "Gastos",
				cing."IngresoEgreso",cing."IdComprobante",cing."Nominal",cing."IdOrden",cing."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",cing."IdPersona"
		FROM v_cartera_ingreso cing
		LEFT JOIN v_Facturacion FAC ON cing."IdComprobante" = FAC."IdComprobante" AND cing."IdTipoComprobante"  IN ('FAE','NCE','FEE')
		UNION
		SELECT  ret."IdEmpresa",ret."EsIngresoManual",ret."IdModulo",ret."Origen",ret."Folio",ret."FechaIngresoRegistro",ret."FechaOperacion",
				ret."FechaVencimientoOperacion",ret."FechaLiquidacion",ret."Plazo",
				ret."IdCondicionLiquidacion",ret."IdEstado",ret."IdCliente",ret."IdPortafolio",ret."IdClienteCorredora",ret."IdPortafolioCorredora",
				ret."IdTipoOperacion",ret."IdTipoProducto",ret."IdConcepto",ret."IdTipoComprobante",ret."FolioComprobante",ret."IdMesa",
				ret."IdClasificacionTipoInstrumento",ret."IdTipoInstrumento",ret."IdInstrumento",ret."FechaEmisionInstrumento",ret."IdMonedaEmision",
				ret."IdMonedaOperacion",ret."IdEmisor",ret."Nemotecnico",ret."EsFungible",ret."IdBolsa",ret."IdBoveda",ret."CodigoAgente",ret."CodigoSucursal",
				ret."IdTipoReajuste",ret."Cantidad",ret."ValorRescate",
				ret."PrecioCompra",ret."TasaCompra",ret."TasaAgente",ret."TasaEmision",ret."MontoCompra",ret."PrecioMercado",ret."CodigoIFRS",
				ret."FolioTransaccion",ret."IdMedioDePago",ret."EmitidoA",
				CASE WHEN ret."IdComprobante" IS NOT NULL THEN ROUND(FAC."Comision" * ret."Cantidad" / FAC."Cantidad",4 ) ELSE ret."Comision" END AS "Comision",
				CASE WHEN ret."IdComprobante" IS NOT NULL THEN ROUND(FAC."Gastos" * ret."Cantidad" / FAC."Cantidad",4 ) ELSE ret."Gastos" END AS "Gastos",
				ret."IngresoEgreso",ret."IdComprobante",ret."Nominal",ret."IdOrden",ret."CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",FAC."FolioComprobante" AS "FolioComprobanteDTE",ret."IdPersona"
		FROM v_cartera_retiro ret
		LEFT JOIN v_Facturacion FAC ON ret."IdComprobante" = FAC."IdComprobante" AND ret."IdTipoComprobante" IN ('FAE','NCE','FEE')
		UNION
		SELECT  tes."IdEmpresa",tes."EsIngresoManual",tes."IdModulo",tes."Origen",tes."Folio",tes."FechaIngresoRegistro",tes."FechaOperacion",
				tes."FechaVencimientoOperacion",tes."FechaLiquidacion",tes."Plazo",
				tes."IdCondicionLiquidacion",tes."IdEstado",tes."IdCliente",tes."IdPortafolio",tes."IdClienteCorredora",tes."IdPortafolioCorredora",
				tes."IdTipoOperacion",tes."IdTipoProducto",tes."IdConcepto",tes."IdTipoComprobante",tes."FolioComprobante",tes."IdMesa",
				tes."IdClasificacionTipoInstrumento",tes."IdTipoInstrumento",tes."IdInstrumento",tes."FechaEmisionInstrumento",tes."IdMonedaEmision",
				tes."IdMonedaOperacion",tes."IdEmisor",tes."Nemotecnico",tes."EsFungible",tes."IdBolsa",tes."IdBoveda",tes."CodigoAgente",tes."CodigoSucursal",
				tes."IdTipoReajuste",tes."Cantidad",tes."ValorRescate",
				tes."PrecioCompra",tes."TasaCompra",tes."TasaAgente",tes."TasaEmision",tes."MontoCompra",tes."PrecioMercado",tes."CodigoIFRS",
				tes."FolioTransaccion",tes."IdMedioDePago",tes."EmitidoA",
				tes."Comision",tes."Gastos",
				tes."IngresoEgreso",tes."IdComprobante",tes."Nominal",0 AS "IdOrden",0 AS "CodigoCorredorContraparte",
				0 AS "SecuenciaEECC",0 AS "FolioComprobanteDTE",tes."IdPersona"
		FROM v_CmbtesTes tes
	) AS SINFO
	LEFT JOIN "BSCL.Portafolio.BD::POR.Portafolio" POR ON POR."IdPortafolio" = SINFO."IdPortafolio"
	LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" MES ON MES."IdPortafolio" = POR."IdPortafolio" AND MES."IdTipoProducto" = 'MERF' AND MES."PorDefecto" = 'S'
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" AS PERS ON PERS."IdPersona" = SINFO."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PER ON PER."IdPersona" = PERS."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Concepto" AS CONCEPTO ON CONCEPTO."IdConcepto" = SINFO."IdConcepto"
	LEFT JOIN "BSCL.Homologacion.BD::HOM.Homologacion" AS hpr 
						ON (hpr."TipoValor" = 'TipoInstrumentoSegunMercado' 
                               AND hpr."Entidad" = 'TipoInstrumentoSegunMercado' 
                               AND hpr."ValorDestino" = SINFO."IdTipoInstrumento"   
                               AND hpr."ValorOrigen" NOT IN ('CM' , 'EN' , 'ET'))
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Empresa" EMPRESA ON SINFO."IdEmpresa" = EMPRESA."IdEmpresa"
	LEFT JOIN "BSCL.SocioNegocio.BD::SOC.SocioDeNegocio" SOC ON SOC."IdSocioDeNegocio" = EMPRESA."IdSocioDeNegocio"
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" AS CORR ON CORR."IdPersona" = SOC."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" AS PCORR ON PCORR."IdPersona" = CORR."IdPersona"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.CondicionDeLiquidacion" AS CODLIQ ON  CODLIQ."IdCondicionDeLiquidacion" = SINFO."IdCondicionLiquidacion"
	LEFT JOIN "BSCL.Plataforma.BD::PLA.Configuracion" AS CONF ON CONF."CodigoParametro" = 'Replica.CodigoPortafolioDefecto' AND CONF."Valor" <> ''
	WHERE ( POR."CodigoPortafolio" IS NULL OR POR."CodigoPortafolio" NOT IN ('APV-A','APV-AN','APV-B','BF','DC-AL','DC-DL','DC-R'))
	and  "Folio"  = -20020

