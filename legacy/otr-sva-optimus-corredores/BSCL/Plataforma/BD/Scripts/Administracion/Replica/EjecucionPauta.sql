--COMIENZO DE PAUTA
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUENTA INVERSION DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CuentaInversion_SaldoFechaEfectivaHist,CuentaInversion_SaldoFechaEfectivaHist;BCS_query:QueryReplicaDelta;Fecha:2025-08-19;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

--DESPUES DE VALORIZACION FFMM
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUSTODIA LIQ ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CustodiaFechaLiquidacionHist;BCS_query:QueryReplicaDelta;Fecha:2025-05-17;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUSTODIA OPE ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CustodiaFechaOperacionHist;BCS_query:QueryReplicaDelta;Fecha:2025-04-26;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CARTERAFM', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CarteraFM;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;


--DESPUES DE PATRIMONIO
INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CuentaInversion_PatrimonioCajaFechaLiquidacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-02;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CuentaInversion_PatrimonioCajaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-02;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_PatrimonioConsolidadoFechaLiquidacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-02;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_PatrimonioConsolidadoFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-30;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;


INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-30;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','PATRIMONIO DIA ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_PatrimonioCustodiaFechaLiquidacion;BCS_query:QueryReplicaDelta;Fecha:2026-05-30;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

--DESPUES DE PATRIMONIO NO HABIL
--INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
--SELECT '','PATRIMONIO DIA +1', 'M', 0,  CURRENT_TIMESTAMP ,
--    'tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2020-09-26;serialDelete:false;truncarDestino:false;serialLaunch:false'
--FROM DUMMY;

--INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
--SELECT '','PATRIMONIO DIA +2', 'M', 0,  CURRENT_TIMESTAMP ,
 --   'tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2020-09-27;serialDelete:false;truncarDestino:false;serialLaunch:false'
--FROM DUMMY;

--INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
--SELECT '','PATRIMONIO DIA +3', 'M', 0,  CURRENT_TIMESTAMP ,
--    'tables:CuentaInversion_PatrimonioCajaFechaLiquidacion,CuentaInversion_PatrimonioCajaFechaOperacion,Custodia_PatrimonioConsolidadoFechaLiquidacion,Custodia_PatrimonioConsolidadoFechaOperacion,Custodia_PatrimonioCustodiaFechaLiquidacion,Custodia_PatrimonioCustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2020-09-20;serialDelete:false;truncarDestino:false;serialLaunch:false'
--FROM DUMMY;

--DESPUES DEL RESPALDO
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CARTERAFMHIS ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CarteraFMHis;BCS_query:QueryReplicaDelta;Fecha:2025-06-09;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CARTERARVHIS ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CarteraRVHis;BCS_query:QueryReplicaDelta;Fecha:2021-11-04;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CARTERARFIFHIS ACTUAL', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CarteraRFIFHis;BCS_query:QueryReplicaDelta;Fecha:2021-04-26;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

--DESPUES DEL CAMBIO DE FECHA
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUENTA INVERSION DIA SIGUIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CuentaInversion_SaldoFechaEfectiva;BCS_query:QueryReplicaDelta;Fecha:2024-12-31;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUENTA INVERSION DIA SIGUIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:CuentaInversion_SaldoFechaMovimiento;BCS_query:QueryReplicaDelta;Fecha:2024-12-31;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;

--DESPUES DE LA VALORIZACION RV
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CARTERARV', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CarteraRV;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

--FIN DE LA PAUTA
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','CUSTODIA DIA SIGUIENTE', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:Custodia_CustodiaFechaLiquidacion,Custodia_CustodiaFechaOperacion;BCS_query:QueryReplicaDelta;Fecha:2025-01-27;serialDelete:false;truncarDestino:false;serialLaunch:false'
FROM DUMMY;



select * from "BSCL.Replica.BD.Vistas::Optimus.SaldoBanco" brbvosb 



select * from bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack" bpbprws 

select * from sys.triggers where TRIGGER_NAME like '%Replica%'

truncate table bscl."BSCL.Plataforma.BD::PLA.Replica.WorkStack" 

drop trigger "BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCartera"
drop trigger "BSCL.Plataforma.BD::PLA.Replica.TriggerReplicaUpdateTablaCuentaInversion_SaldoFechaMovimiento"

select * from 
"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica"
where "IdPersona"  = 30371


select * from 
"BSCL.SocioNegocio.BD.Vistas::PER.ConsultarPersonaDireccionElectronicaAnalitica"
where "IdPersona"  = 30371


select * from SocioNegocio_PersonaDireccionElectronica snpde 
where IdPersona = 30371


delete from SocioNegocio_PersonaDireccionElectronica  
where IdPersona = 30371


SELECT "NombreClasePrograma" FROM BSCL."BSCL.Plataforma.BD::BAT.TipoPrograma" WHERE "IdTipoPrograma"='PROCESONOMINACIRCULARIZACION'


select * from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPortafolio"
where "IdPortafolio"  = 2826 and "IdTipoIndicador" = 64


select "FechaCustodia",count(1) from "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion" bcbhcfo 
group by "FechaCustodia" 


select "Nemotecnico" ,"PrecioMercadoCustodia" ,* from "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion" bcbhcfo 
where "Nemotecnico" like '%SM-CHILE%' AND "FechaCustodia" = '2025-07-31'


select * from "BSCL.Custodia.BD::HIS.CustodiaFechaOperacion" bcbhcfo 
where "Nemotecnico" = 'SM-CHILE' AND "FechaCustodia" = '2025-07-31'



SELECT FECHACOMPROBANTE ,count(1) FROM 
"BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago" brbvrtcfdp 
group by FECHACOMPROBANTE 
order by FECHACOMPROBANTE desc



SELECT count(1) FROM 
"BSCL.Replica.BD.Vistas::ReplicaTesoreria_ComprobanteFormaDePago" brbvrtcfdp 
where FechaComprobante > '2022-01-01'



select "Estado" ,* from "BSCL.Mesa.BD.Vistas::SPT.ConsultarOperacionesSpotCliente"



truncate table "BSCL.Orden.BD::MRV.PUNTAS_RV" 

truncate table "BSCL.Orden.BD::MRV.PALOS_RV" 



select "lastLogin" ,o.* from ousr o order by o."lastLogin"  desc



select * from "BSCL.Orden.BD::ORD.ControlMercado" bobocm 
where "Procesado" = 'R'


select * from ordorden
