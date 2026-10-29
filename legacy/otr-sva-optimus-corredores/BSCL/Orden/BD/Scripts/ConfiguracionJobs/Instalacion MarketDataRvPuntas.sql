SET SCHEMA "BSCL";

SELECT * FROM "BSCL"."BSCL.Orden.BD::MRV.PUNTAS_RV";

select * from "BSCL"."BSCL.Plataforma.BD::BAT.TipoPrograma" where "Modulo" like '%Ord%'

INSERT INTO "BSCL.Plataforma.BD::BAT.TipoPrograma"("IdTipoPrograma","Descripcion","MaximoIntentos","Modulo","NombreClasePrograma","TipoClasePrograma","Nodo","EsPauta")
SELECT 'MarketDataRvPuntas' as "IdTipoPrograma",'MarketDataRvPuntas' AS "Descripcion",1 AS "MaximoIntentos",'Ordenes' AS "Modulo",
'cl.bcs.ordenes.jobs.MarketDataRvPuntas' AS"NombreClasePrograma",'JAVA' AS "TipoClasePrograma",'D' as "Nodo",'N' AS "EsPauta" FROM DUMMY

----PUNTAS RV------------------------------------------------------------------------------------------------------------------------------
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlPUNTAS_RV";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlPUNTAS_RV"              
AFTER INSERT ON "BSCL.Orden.BD::MRV.PUNTAS_RV"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('PUNTA_RV',:mensaje."timesTamp",:mensaje."symbol",CURRENT_TIMESTAMP,'N');
END;
--
DROP TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlPUNTAS_RV_BVL";

CREATE TRIGGER "BSCL.Orden.BD.Triggers::CrearMensajeControlPUNTAS_RV_BVL"              
AFTER INSERT ON "BSCL.Orden.BD::MRV.PUNTAS_RV"
REFERENCING NEW ROW mensaje
FOR EACH ROW
BEGIN 
	
	INSERT INTO "BSCL.Orden.BD::ORD.ControlMercado"
	("TipoMensaje","IdMensaje1","IdMensaje2","FechaIngresoRegistro","Procesado")
	VALUES ('PUNTA_RV_BVL',:mensaje."timesTamp",:mensaje."symbol",CURRENT_TIMESTAMP,'N');
END;

SELECT * FROM "BSCL.Plataforma.BD::BAT.Agenda"
DROP TABLE #TT;
CREATE LOCAL TEMPORARY TABLE #TT (
	"NombreParametro" nvarchar(100) , "Valor" nvarchar(100)
);

CALL "BSCL.Plataforma.BD.Servicios::BAT.AgregarAgenda"(
	p_IdEmpresa          => 1,
	p_IdUsuario          => 0,
	p_IdModulo          => 'PLA',
	p_IdTipoTarea		=> 'MarketDataRvPuntas',
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
