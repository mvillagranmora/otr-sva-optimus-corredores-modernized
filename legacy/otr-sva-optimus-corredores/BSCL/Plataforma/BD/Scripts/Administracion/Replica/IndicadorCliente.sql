INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'SocioNegocio_IndicadorCliente', 'I', 0,  CURRENT_TIMESTAMP  
FROM "BSCL.Replica.BD.Vistas::ReplicaSocioNegocio_IndicadorCliente" i
--where i.IDTIPOINDICADOR <= 70
--where i.IDTIPOINDICADOR > 70 and i.IDTIPOINDICADOR <= 20017
where i.IDTIPOINDICADOR > 20017

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'INDICADORES_CLIENTE', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'INDICADORES_CUENTA', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'INDICADOR', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADOR";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'DETALLE_INDICADOR', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaDETALLE_INDICADOR";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'TBCTMFRV', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaTBCTMFRV";

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'TBCTMSCF', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaTBCTMSCF";

select SEC_MOVTO_FAC ,COUNT(1) from "BSCL.Replica.BD.Vistas::ReplicaTBCTMFRV"
GROUP BY SEC_MOVTO_FAC 
HAVING COUNT(1) > 1

delete from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" where "Table" like '%Indicador%'



select "Table" ,count(1) 
from "BSCL.Plataforma.BD::PLA.Replica.WorkStack" where "Status"  = 0
and "Stamp" >= '2024-10-09 17:50:00'
group by "Table" 

select * from "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresPersonaAnalitica" bpbvicica 

SELECT per_id,CUENTA,fil_id,ind_id,count(1)  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA"
group by per_id,CUENTA,fil_id,ind_id
having count(1) >1

SELECT per_id,fil_id,ind_id,count(1)  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE"
group by per_id,fil_id,ind_id
having count(1) >1

select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" where din_id is null

select * 
FROM "BSCL.Portafolio.BD::POR.Portafolio" POR
	LEFT JOIN "BSCL.SocioNegocio.BD::CLI.Cliente" as CLI ON POR."IdCliente" = CLI."IdCliente"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.Persona" PER ON CLI."IdPersona" = PER."IdPersona"
	LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as DOC ON (DOC."IdDocumentoIdentidad" = PER."IdDocumentoIdentidad" )
	LEFT JOIN "BSCL.Plataforma.BD::IND.TipoIndicador" as TIND ON (TIND."IdTipoEntidad" = 'PORTAFOLIO' OR TIND."CodigoTipoIndicador" IN('CCGF','ANTLEGAL','CONAPV','CONPRESADM','CS','CVC','CCI','DECRESID','FOTOCOPIA','ENTREGACLAVE','CDFYNSA','AIPFYNSA','REOP','REOE','TCLICMFJ','CM'))AND TIND."IdEstado" != 'INA'
	LEFT JOIN "BSCL.Plataforma.BD::IND.Indicador" AS INDPROPIO ON TIND."IdTipoEntidad" = 'PORTAFOLIO' and INDPROPIO."IdEntidad" = POR."IdPortafolio" AND INDPROPIO."IdTipoIndicador" = TIND."IdTipoIndicador"
where tind."IdTipoIndicador"  = 	35 and "CodigoPortafolio"  = 0 and doc."DocumentoIdentidad"  like '%76192568%'

delete from "BSCL.Plataforma.BD::IND.Indicador" where "IdTipoIndicador"  = 68 and "IdTipoEntidad"  = 'CLIENTE'

select ind_id,count(1) from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" brbvric group by IND_ID 

select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" where PER_ID  = 11701

select count(1) from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA" brbvric 

select count(1) from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" brbvric 

SELECT per_id,fil_id,ind_id,count(1)  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE"
group by per_id,fil_id,ind_id
having count(1) >1

select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CUENTA" brbvri where IND_ID = 57

select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" brbvric  


select * from "BSCL.Replica.BD.Vistas::ReplicaINDICADOR" brbvric  

select distinct i1.ind_id 
from "BSCL.Replica.BD.Vistas::ReplicaINDICADORES_CLIENTE" i1
left join "BSCL.Replica.BD.Vistas::ReplicaINDICADOR" i2 on i1.IND_ID  = i2.IND_ID  
where i2.ind_id is null

2
13
14
15
18
21
26
27
33
34
35
36
43
51
57
62
64
66
67
68
76
77
78
81
112
113
116
117
118
20105

SELECT * FROM "BSCL.Homologacion.BD::HOM.Homologacion" WHERE "ValorDestino"  ='CM'







INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT table_key,'INDICADOR', 'I', 0,  CURRENT_TIMESTAMP  
from "BSCL.Replica.BD.Vistas::ReplicaINDICADOR"

SELECT "IdTipoIndicador",COUNT(1) 
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica"
GROUP BY "IdTipoIndicador"
ORDER BY "IdTipoIndicador"

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp","Parameters")
SELECT '','INDICADORPORTAFOLIO', 'M', 0,  CURRENT_TIMESTAMP ,
    'tables:SocioNegocio_IndicadorPortafolio;BCS_query:QueryReplica;Fecha:2020-10-01;serialDelete:false;truncarDestino:true;serialLaunch:false'
FROM DUMMY;

SELECT * FROM "BSCL.Plataforma.BD::PLA.Replica.WorkStack" WHERE "Action" = 'I'


SELECT "IdTipoIndicador" , count(1)
FROM "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresClienteAnalitica"
group by "IdTipoIndicador" 


SELECT "Table","Action", MIN("Stamp"), count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2022-09-22' AND
"Status" = 0-- 0AND "Action" = 'D'
GROUP BY "Table","Action"

update BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
set "Stamp" = CURRENT_TIMESTAMP
where "Table"  = 'SocioNegocio_IndicadorCliente'

SELECT "Table",count(1)
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Stamp" >= '2023-11-27'
AND "Status" = 0
GROUP BY "Table"

SELECT *
FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE "Stamp" >= '2020-08-27' 
AND "Table" = 'SocioNegocio_IndicadorCliente'



DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"
WHERE
"Stamp" >= '2020-08-20' AND
"Status" = 0 --"Action" = 'D'
AND "Table" = 'Custodia_CarteraRVHis'


DELETE FROM BSCL."BSCL.Plataforma.BD::PLA.Replica.WorkStack"



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdTipoRelacion" AS varchar(200))  ,'SocioNegocio_TipoRelacion', 'I', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/TipoRelacion";



INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT cast("IdEmpleadoSupervisor" AS varchar(200))  ,'SocioNegocio_Supervisor', 'I', 0,  CURRENT_TIMESTAMP  
FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Supervisor";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdEmpleadoEjecutivo" AS varchar(200)),'SocioNegocio_Ejecutivo', 'I', 0,  CURRENT_TIMESTAMP  
	FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Ejecutivo";
	

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdEmpleado" AS varchar(200)),'SocioNegocio_Empleado', 'I', 0,  CURRENT_TIMESTAMP 
	FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/Empleado";
	

INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdValorCobro" AS varchar(200))  ,'Plataforma_ValorCobro', 'I', 0,  CURRENT_TIMESTAMP 
	FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ValorCobro";
	
INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdConcepto" AS varchar(200)),'Plataforma_Concepto', 'I', 0,  CURRENT_TIMESTAMP 
	FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/Concepto";
	

  	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdDireccionElectronica" AS varchar(200)),'SocioNegocio_PersonaDireccionElectronica', 'I', 0,
CURRENT_TIMESTAMP  FROM "_SYS_BIC"."BSCL.SocioNegocio.BD.Analytics/PersonaDireccionElectronica";


	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdRegimenFondoMutuo" AS varchar(200)) || ':' || cast("IdSerieFondo" AS varchar(200)),'FondoMutuo_RegimenSerieFondo', 'I', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.FondoMutuo.BD.Analytics/RegimenSerieFondo";


 	INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
	SELECT cast("IdParametroFinanciero" AS varchar(200)) || ':' || cast("Fecha" AS varchar(200)),'Plataforma_ParametroFinanciero', 'I', 0,  CURRENT_TIMESTAMP 
FROM "_SYS_BIC"."BSCL.Plataforma.BD.Analytics/ParametroFinanciero";


INSERT INTO "BSCL.Plataforma.BD::PLA.Replica.WorkStack"("Key", "Table","Action" , "Status", "Stamp"  )
SELECT DISTINCT cast("IdPortafolio" AS varchar(200)) || ':' ||  cast("Nemotecnico" AS varchar(200)),'Custodia_CustodiaFechaLiquidacion','I', 0,  
CURRENT_TIMESTAMP  FROM bscl."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
	

SELECT COUNT(1) FROM bscl."BSCL.Custodia.BD::HIS.CustodiaFechaLiquidacion"
SELECT COUNT(1) FROM bscl."BSCL.Custodia.BD::HIS.CustodiaFechaOperacion"

select * from "BSCL.Replica.BD.Vistas::Sebra.TBTECMBT" brbvst 


