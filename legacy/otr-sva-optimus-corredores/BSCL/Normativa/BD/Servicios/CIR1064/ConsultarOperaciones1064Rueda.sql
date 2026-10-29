SET SCHEMA "BSCL";
 

DROP PROCEDURE "BSCL.Normativa.BD.Servicios.CIR1064::ConsultarOperaciones1064Rueda";

--call "BSCL.Normativa.BD.Servicios.CIR1064::ConsultarOperaciones1064Rueda"(2782,0,?)

CREATE PROCEDURE 

"BSCL.Normativa.BD.Servicios.CIR1064::ConsultarOperaciones1064Rueda" ( 
    --VARIABLES DEL PROCESO
	--IN p_IdEmpresa				  BIGINT,
	IN p_IdSolicitudCircular	  BIGINT,	
	IN p_IdProceso				  BIGINT default 0,	
--	IN p_Fecha					  DATE,
    --VARIABLES DE SALIDA 
	OUT p_out_TablaSalida	"BSCL.Normativa.BD::NOR.SALIDA_OPERACION1064RUEDA" --Se debe llamar siempre p_out_TablaSalida
)
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		
DECLARE v_IdNormativa 	NVARCHAR(15):= 'CIR1064';   -- 'CIR63';		

	
declare v_CodCorr , v_RutCorr 	NVARCHAR(15); 

declare v_Log BIGINT default 0;
declare v_CondRUEDA  BIGINT default 0;

		
declare v_Campos1064R table 
(
	"Fecha" 					NVARCHAR(10) 
	,"Corredor"					NVARCHAR(3) 
	,"NemoCorr"					NVARCHAR(10) 
	,"RutCorr"					NVARCHAR(11) 
	,"RutCli"					NVARCHAR(11) 
	,"SecuenciaCli"				NVARCHAR(2) 
	,"NemoIns"					NVARCHAR(10) 
	,"TipoIns"					NVARCHAR(2) 
	,"Rueda"					NVARCHAR(1) 
	,"TipoRel"					NVARCHAR(1)
	,"TipoOpe"					NVARCHAR(2) 
	,"FolioTrans"				NVARCHAR(8) 
	,"CantidadTrans"			NVARCHAR(15) 
	,"MontoTrans"				NVARCHAR(15) 
		
);
		
		
		

DECLARE v_fechaHoraSistema		NVARCHAR(40);

DECLARE v_FechaOperacionDesde 	NVARCHAR(12) default NULL;
DECLARE v_FechaOperacionHasta 	NVARCHAR(12) default NULL;
DECLARE v_FechaOperacion 		NVARCHAR(12) default NULL;
DECLARE v_Fecha			 		NVARCHAR(12) default NULL;
DECLARE v_CondFechaHasta 		BIGINT default 0;
DECLARE v_CondFecha             BIGINT default 0;
DECLARE v_CondUsu               BIGINT default 0;
DECLARE v_UsuSistema            BIGINT default 0;
DECLARE v_IdEmpresa 		    BIGINT default 0;
DECLARE v_IdDocumento 		    NVARCHAR(15) default '';






IF p_IdSolicitudCircular>0 THEN
		
			SELECT TOP 1 "IdDocumento" INTO v_IdDocumento 
			FROM "BSCL.Normativa.BD::NOR.SolicitudCircular"
			WHERE "IdSolicitudCircular" = p_IdSolicitudCircular;
			
			SELECT TOP 1 "IdEmpresa" INTO v_IdEmpresa 
			FROM "BSCL.Normativa.BD::DOC.Documento"
			WHERE "IdDocumento" = v_IdDocumento;
		
			
			
			v_Datos = SELECT 
				  CASE WHEN SOL."TipoParametro"='FINI'	THEN "Valor" ELSE NULL END AS "FINI"
				FROM "BSCL.Normativa.BD::NOR.SolicitudParametros" SOL
			 	WHERE SOL."IdSolicitudCircular" = p_IdSolicitudCircular;
				
END IF;

	v_DatosAgrupado =SELECT  
	  MAX(SOL."FINI") 	AS "FINI"
	
	FROM :v_Datos SOL;
					

   SELECT TOP 1 "FINI"  INTO v_Fecha FROM :v_DatosAgrupado;	



select count(1) into v_CondUsu from "BSCL.Plataforma.BD::ACC.Usuario" where "CodigoUsuario" = 'SISTEMA' and "IdEmpresa" = v_IdEmpresa;

IF(v_CondUsu>0)
THEN
select top 1 "IdUsuario" into v_UsuSistema from "BSCL.Plataforma.BD::ACC.Usuario" where "CodigoUsuario" = 'SISTEMA' and "IdEmpresa" = v_IdEmpresa;
END IF;


/*
call "BSCL.Plataforma.BD.Utilitarios::ObtenerFechaHoraSistema"(
p_IdEmpresa => 1,
p_out_FechaHoraSistema => v_fechaHoraSistema
);
*/

--v_fechaHoraSistema = CURRENT_UTCTIMESTAMP;

---- Transacciones desde servicionNormativo de Operaciones

--v_TablaModulo = Select '' as "IdModulo" from dummy where 1=2;


v_registroParametro = SELECT 'FECHA' AS "Codigo", v_Fecha AS "Valor" FROM DUMMY; --where 1=2
		
			
v_TablaModulo = Select 'FAC' as "IdModulo" from dummy;

CALL "BSCL.Normativa.BD.Servicios.NOR::ConsultarOperaciones"
(
	p_IdEmpresa			=> v_IdEmpresa,
	p_IdNormativa		=> v_IdNormativa,
	p_tablaModulo		=> :v_TablaModulo,
	p_registroParametro	=> :v_registroParametro,
	p_out_TablaSalida	=> v_SalidaConsultarOperaciones
);

p_DetalleLog = 
(Select 
	''		 as "SubRegistro",
	''  	 as "CampoRegistro",
	''  	 as "ValorAntiguo",
	'' 		 as "ValorNuevo" 
FROM dummy);


CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
p_IdTipoLog => 'INICIO' 
,p_IdModulo => 'NOR' 
,p_IdUsuario => v_UsuSistema 
,p_IdEmpresa => v_IdEmpresa 
,p_FechaSistemaLog => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD')
,p_IdUsuarioAtendido => v_UsuSistema
,p_IdTipoRegistro => 'CIR1064R'--tiponormativa
,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
,p_NombreProceso => 'GenCir1064R'-- hacia abajo opcionales
,p_IdRegistro => ''
,p_IdProceso=>0  --:p_IdProceso
,p_EstadoProceso => ''
,p_EstadoRegistro=> ''
,p_IdTipoDocumentoIdentidad=> ''
,p_IdDocumentoIdentidad=> ''					
);


	
SELECT "E"."Codigo" ,"DOC"."DocumentoIdentidad" INTO v_CodCorr , v_RutCorr    
FROM "BSCL.Plataforma.BD::PLA.Empresa" AS "E"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::SOC.ConsultarSocioDeNegocio" AS "CSN" ON "CSN"."IdSocioDeNegocio" = "E"."IdSocioDeNegocio"
INNER JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" AS "CC" ON "CC"."IdCliente" = "CSN"."IdCliente"
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" on ( "DOC"."IdPersona" = "CSN"."IdPersona")
WHERE "E"."IdEmpresa" = :v_IdEmpresa;

	
	
--select * from :v_SalidaConsultarOperaciones;

v_Campos_Homologacion =
select * from "BSCL.Normativa.BD::DOC.Homologacion" where "Esquema" = 'CIR1064R';


--filtro los intrumentos que necesito para elaborar informe
select count(1) into v_CondRUEDA from :v_SalidaConsultarOperaciones where "IdTipoInstrumento" in ('DOL','OSA','MIL','MON','ETX','ETN','CFX','CFM','CFI','ACX','ACN'); 


IF(v_CondRUEDA>0)
THEN
v_Campos1064RUEDA = 
select 
"FechaMovimiento","FolioTransacion","SecuenciaOrden","IdOperacion","IdTipoOperacion","IdCliente","IdPortafolio","IdClasificacionTipoInstrumento","Mercado","IdEmisor","Nemotecnico","Cantidad","Precio","Tasa","IdMoneda","IdInstrumento","IdTipoInstrumento","IdTipoProducto",
"IdTransaccion","FechaTransaccion","TasaCompra","RelacionInstrumento","IdContraparte"
,"IdOperadorContraparte","IdOperadorCorredor","IdTipoComprobante","FolioComprobante","ValorPresenteCompraInst","FechaVencimiento"
,"MontoCompra","MontoVenta","Comision","Derecho","CargoFijo","IVA","TotalCompra","TotalVenta","IdCondicionLiquidacion","IdDocumentoRelacion","FechaVencimientoPacto","PrecioPlazo","TasaPlazo","IdMonedaPlazo","TotalCompraVencimiento","TotalVentaVencimiento","Custodia","IdBolsa","IdBoveda","CantidadGarantia","NemoSVS"
,"TasaVenta","ValorPresente","UtilidadPerdida","FechaOrigenPacto","DescripcionOperacion","IdEstadoFactura","Canal"
,"ValorIVA","FolioComprobanteAsociado","ModalidadEjecucion","EsFungible","IdMovimientoRelacionado","CodigoOperadorContraparte"
,"CodigoOperadorCorredor","TipoOperacionSecundaria","ValorRescateInicial","IdMonedaSecundaria","IdNormativa","IdTipoDocumento"
,"HIdTipoProducto","HIdClasificacionTipoInstrumento","HIdTipoInstrumento","HIdTipoOperacion","EsEnRueda"
from :v_SalidaConsultarOperaciones where "IdTipoInstrumento" in ('DOL','OSA','MIL','MON','ETX','ETN','CFX','CFM','CFI','ACX','ACN'); 
--select count(1) as rueda from :v_Campos1064RUEDA;

ELSE
v_Campos1064RUEDA = 
select 
'' as "FechaMovimiento",'' as "FolioTransacion"	,'' as "SecuenciaOrden",0  as "IdOperacion",'' as "IdTipoOperacion",0  as "IdCliente",0  as "IdPortafolio",'' as "IdClasificacionTipoInstrumento" ,'' as "Mercado" ,'' as "IdEmisor" ,'' as "Nemotecnico" ,0  as "Cantidad" ,0  as "Precio" ,0  as "Tasa" ,'' as "IdMoneda",0 as "IdInstrumento" ,'' as "IdTipoInstrumento" ,'' as "IdTipoProducto" ,0 as "IdTransaccion",'' as "FechaTransaccion" ,0  as "TasaCompra" ,'' as "RelacionInstrumento" ,0  as "IdContraparte"
,0  as "IdOperadorContraparte" ,0  as "IdOperadorCorredor" ,'' as "IdTipoComprobante" ,0  as "FolioComprobante" ,0  as "ValorPresenteCompraInst" ,'' as "FechaVencimiento"
,0  as "MontoCompra" ,0  as "MontoVenta" ,0  as "Comision" ,0  as "Derecho" ,0  as "CargoFijo" ,0  as "IVA" ,0  as "TotalCompra" ,0  as "TotalVenta" ,'' as "IdCondicionLiquidacion" ,0  as "IdDocumentoRelacion" ,'' as "FechaVencimientoPacto" ,0  as "PrecioPlazo" ,0  as "TasaPlazo" ,'' as "IdMonedaPlazo" ,0  as "TotalCompraVencimiento" ,0  as "TotalVentaVencimiento" ,'' as "Custodia" ,'' as "IdBolsa" ,'' as "IdBoveda" ,0  as "CantidadGarantia" ,'' as "NemoSVS"
,0  as "TasaVenta" ,0  as "ValorPresente",0  as "UtilidadPerdida" ,'' as "FechaOrigenPacto" ,'' as "DescripcionOperacion" ,'' as "IdEstadoFactura" ,'' as "Canal"
,0  as "ValorIVA" ,0  as "FolioComprobanteAsociado" ,'' as "ModalidadEjecucion" ,'' as "EsFungible" ,0  as "IdMovimientoRelacionado" ,'' as "CodigoOperadorContraparte"
,'' as "CodigoOperadorCorredor" ,'' as "TipoOperacionSecundaria" ,0  as "ValorRescateInicial" ,'' as "IdMonedaSecundaria" ,'' as "IdNormativa" ,'' as "IdTipoDocumento"
,'' as "HIdTipoProducto" ,'' as "HIdClasificacionTipoInstrumento" ,'' as "HIdTipoInstrumento" ,'' as "HIdTipoOperacion" ,'' as "EsEnRueda"from dummy where 1=2 ;		

END IF;



v_Campos1064R = 
--p_out_TablaSalida = 

select 

"RUE"."FechaMovimiento" as "Fecha", 
IFNULL(v_CodCorr,'') as "Corredor",
'' as "NemoCorr",	
IFNULL(v_RutCorr,'')  as "RutCorr",
"DOC"."DocumentoIdentidad" as "RutCli",
'' as "SecuenciaCli",
"RUE"."Nemotecnico" as "NemoIns",
'03' as "TipoIns",    
'R' as "Rueda",
IFNULL("VIN"."ValorDestino",'') as "TipoRel",	
LEFT("RUE"."IdTipoOperacion",1) as  "TipoOpe",	
IFNULL(RIGHT('0000000'|| RTRIM(LTRIM("RUE"."FolioTransacion")),7),'0000000')
as "FolioTrans",	
CAST(to_integer(IFNULL("RUE"."Cantidad",000.00))AS VARCHAR) || RIGHT(CAST(TO_DECIMAL(IFNULL("RUE"."Cantidad",000.00), 10, 2) AS VARCHAR),2)
as "CantidadTrans",	

case LEFT("RUE"."IdTipoOperacion",1)       
	 when 'C' then "RUE"."MontoCompra" --CAST(to_integer(TO_DECIMAL(IFNULL("RUE"."MontoCompra",000.00)))AS VARCHAR) || RIGHT(CAST(TO_DECIMAL(IFNULL("RUE"."MontoCompra",000.00), 10, 2)AS VARCHAR),2)--CAST(to_integer(IFNULL("RUE"."MontoCompra",000.00))AS VARCHAR) || RIGHT(CAST(TO_DECIMAL(IFNULL("RUE"."MontoCompra",000.00), 10, 2) AS VARCHAR),2)
	 when 'V' then "RUE"."MontoVenta" --CAST(to_integer(IFNULL("RUE"."MontoVenta",000.00))AS VARCHAR) || RIGHT(CAST(TO_DECIMAL(IFNULL("RUE"."MontoVenta",000.00), 10, 2) AS VARCHAR),2)
else 0
end
as "MontoTrans"
from :v_Campos1064RUEDA as "RUE"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as "CC" on ("RUE"."IdCliente" = "CC"."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" on ( "DOC"."IdDocumentoIdentidad" = "CC"."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "IND" on ("RUE"."IdCliente" = "IND"."IdCliente")
LEFT JOIN :v_Campos_Homologacion as "VIN" on ("VIN"."ValorOrigen" = "IND"."ValorIndicador" and "VIN"."Esquema" = 'CIR1064R' and "VIN"."Tipo"='Vinculacion')
where "IND"."NombreTipoIndicador" = 'Tipo Relacion Corredora (CI 1064)';




--//////////////////////////////////////////VALIDACIONES - AQUI/////////////////////////////////////


--agregar validaciones para llenar log
v_TablaLog = 

select 
1 as "IdTipoLogNormativa",
14 as "IdUsuario",
TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD') as "FechaLog",
0 as "IdProceso",
IFNULL(p_IdSolicitudCircular,0) as "IdSolicitudCircular",
"RUE"."FolioTransacion" as "IdOperacion",

"RUE"."FechaMovimiento" 
|| 
IFNULL(v_CodCorr,'')
||
'' 
||	
IFNULL(v_RutCorr,'')  
||
"DOC"."DocumentoIdentidad" 
||
'' 
||
"RUE"."Nemotecnico" 
||
'03' 
||
'R' 
||
IFNULL("VIN"."ValorDestino",'-') 
||	
LEFT("RUE"."IdTipoOperacion",1)
||	
IFNULL(RIGHT('0000000'|| RTRIM(LTRIM("RUE"."FolioTransacion")),7),'0000000')
||	
"RUE"."Cantidad" 
||
case LEFT("RUE"."IdTipoOperacion",1)        
	 when 'C' then "RUE"."MontoCompra" 
	 when 'V' then "RUE"."MontoVenta"
else 0
end

 as "Registro",
'' as "Observacion"

from :v_Campos1064RUEDA as "RUE"
LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" as "CC" on ("RUE"."IdCliente" = "CC"."IdCliente")
LEFT JOIN "BSCL.SocioNegocio.BD::PER.DocumentoIdentidad" as "DOC" on ( "DOC"."IdDocumentoIdentidad" = "CC"."IdDocumentoIdentidad")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::IND.ConsultarIndicadoresCliente" as "IND" on ("RUE"."IdCliente" = "IND"."IdCliente")
LEFT JOIN :v_Campos_Homologacion as "VIN" on ("VIN"."ValorOrigen" = "IND"."ValorIndicador" and "VIN"."Esquema" = 'CIR1064R' and "VIN"."Tipo"='Vinculacion')
where "IND"."NombreTipoIndicador" = 'Tipo Relacion Corredora (CI 1064)'
and 1=2;



--insert a log normativo
 
--
CALL "BSCL.Normativa.BD.Servicios.NOR::AgregarLogNormativo"(p_IdEmpresa=>1,p_InfoDetalle=>:v_TablaLog,p_out_resultado=>v_Log);


CALL "BSCL.Plataforma.BD.Servicios::AgregarLog" (
p_IdTipoLog => 'TERMINO' 
,p_IdModulo => 'NOR' 
,p_IdUsuario => v_UsuSistema
,p_IdEmpresa => v_IdEmpresa 
,p_FechaSistemaLog => TO_VARCHAR (CURRENT_UTCTIMESTAMP, 'YYYY-MM-DD')
,p_IdUsuarioAtendido => v_UsuSistema
,p_IdTipoRegistro => 'CIR1064FR'--tiponormativa
,p_DetalleLog => :p_DetalleLog	-- hacia arriba obligatorios
,p_NombreProceso => 'GenCir1064R'-- hacia abajo opcionales
,p_IdRegistro => ''
,p_IdProceso=> 0   --:p_IdProceso
,p_EstadoProceso => ''
,p_EstadoRegistro=> ''
,p_IdTipoDocumentoIdentidad=> ''
,p_IdDocumentoIdentidad=> ''					
);

/*
p_out_TablaSalida = 
SELECT
LEFT("Fecha",10) 			as  "Fecha" 		
,RPAD("Corredor",3,'0') 	as "Corredor"		 
,RPAD("NemoCorr",10,' ') 	as "NemoCorr"	 	
,RPAD("RutCorr",11,' ') 	as "RutCorr"		 
,RPAD("RutCli",11,' ') 		as "RutCli"			
,LPAD("SecuenciaCli",2,'0')	as "SecuenciaCli"
,RPAD("NemoIns",10,' ') 	as  "NemoIns"		
,RPAD("TipoIns",2,' ') 		as "TipoIns"		
,RPAD("Rueda",1,' ') 		as "Rueda"		
,RPAD("TipoRel",1,' ') 		as "TipoRel"	
,RPAD("TipoOpe",2,' ') 		as "TipoOpe"
,LPAD("FolioTrans",8,'0') 	as "FolioTrans"	
,LPAD("CantidadTrans",15,'0') as "CantidadTrans"
,LPAD("MontoTrans",15,'0') 	as "MontoTrans"
FROM :v_Campos1064R ;
*/

p_out_TablaSalida = 
SELECT
LEFT(COALESCE("Fecha",''),10) 				
||RPAD(COALESCE("Corredor",''),3,'0') 		 
||RPAD(COALESCE("NemoCorr",''),10,' ')  	
||RPAD(COALESCE("RutCorr",''),11,' ') 		 
||RPAD(COALESCE("RutCli",''),11,' ') 			
||LPAD(COALESCE("SecuenciaCli",''),2,'0')	
||RPAD(COALESCE("NemoIns",''),10,' ') 	
||RPAD(COALESCE("TipoIns",''),2,' ') 		
||RPAD(COALESCE("Rueda",''),1,' ') 			
||RPAD(COALESCE("TipoRel",''),1,' ') 			
||RPAD(COALESCE("TipoOpe",''),2,' ') 		
||LPAD(COALESCE("FolioTrans",''),8,'0') 
||LPAD(COALESCE("CantidadTrans",''),15,'0') 
||LPAD(COALESCE("MontoTrans",''),15,'0') as "Registro"	
FROM :v_Campos1064R ;
/*
select 
"Fecha" 		
||"Corredor"		 
||"NemoCorr"	 	
||"RutCorr"		 
||"RutCli"			
||"SecuenciaCli"
||"NemoIns"		
||"TipoIns"		
||"Rueda"		
||"TipoRel"	
||"TipoOpe"
||"FolioTrans"	
||"CantidadTrans"
||"MontoTrans"
from :p_out_TablaSalida;

select * from :v_Campos1064RUEDA  ;
*/

select "Registro" as "Registro" from :p_out_TablaSalida;

	
END; 
