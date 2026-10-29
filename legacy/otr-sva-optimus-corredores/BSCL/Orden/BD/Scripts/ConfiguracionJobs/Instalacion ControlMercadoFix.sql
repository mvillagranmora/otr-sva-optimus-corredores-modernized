SET SCHEMA "BSCL";
GO

DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlTBRESPUESTAS_AGENTE";
GO
CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlTBRESPUESTAS_AGENTE"              
AFTER INSERT ON "BSCL.Orden.BD::DMA.TBRESPUESTAS"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('RESPUESTA_DMA_AGENTE',:mensaje."NRO_ENTRADA",:mensaje."FECHA",CURRENT_TIMESTAMP,'N');
END;
GO
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlTBRESPUESTAS_RETAIL";
GO
CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlTBRESPUESTAS_RETAIL"              
AFTER INSERT ON "BSCL.Orden.BD::DMA.TBRESPUESTAS_2"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('RESPUESTA_DMA_RETAIL',:mensaje."NRO_ENTRADA",:mensaje."FECHA",CURRENT_TIMESTAMP,'N');
END;
GO


--TIPO PROGRAMA
INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'ControlMercadoFix' as "IdTipoPrograma",'ControlMercadoFix' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.ControlMercadoFix' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY
GO
COMMIT
--AGENDA
DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);
GO
INSERT INTO #TT
select 'IdEmpresa' AS "NombreParametro", '1' as "ValorParametro" FROM DUMMY;
GO
INSERT INTO #TT
select 'IdUsuario' AS "NombreParametro", '0' as "ValorParametro" FROM DUMMY;
GO
INSERT INTO #TT
select 'clienteFix' AS "NombreParametro", 'AGENTE' as "ValorParametro" FROM DUMMY;
GO
--INSERT INTO #TT
--select 'clienteFix' AS "NombreParametro", 'RETAIL' as "ValorParametro" FROM DUMMY;


SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.Agenda"

SELECT * FROM "BSCL"."BSCL.Plataforma.BD::BAT.ParametrosAgenda"


CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'ControlMercadoFix',
	p_EsDemonio			=> 'S',  
	p_DiaDeMes			=> '**',
	p_DiaDeSemana		=> '*',
	p_Mes				=> '**',
	p_Hora				=> '**',
	p_Minuto			=> '01',
	p_SoloDiasHabiales	=> 'N',
	p_SoloPrincipioMes	=> 'N',
	p_SoloFinDeMes		=> 'N',
	p_habilitado		=> 'S',
	p_TablaParametros	=> #TT,
	
	p_IdAgenda					=> ?,
	p_IdPrograma				=> ?,
	p_out_AdvertenciasContador 	=> ?,
	p_out_AdvertenciasDetalle 	=> ?,
	p_out_ErroresContador 		=> ?,	
	p_out_ErroresDetalle		=> ?);
	
	commit;
	
	
