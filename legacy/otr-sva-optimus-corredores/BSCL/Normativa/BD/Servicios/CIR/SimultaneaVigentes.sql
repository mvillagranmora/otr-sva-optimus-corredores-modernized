SET SCHEMA "BSCL";
DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.SimultaneaVigentes";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.SimultaneaVigentes" ( 
    IN p_IdEmpresa				BIGINT,
    IN p_FechaConsulta			NVARCHAR(10),			 --'YYYY-MM-DD'	
    IN p_cliente                TABLE("IdCliente" BIGINT,"IdPortafolio" BIGINT),
    IN p_fechaHoraSistema       NVARCHAR(40),
    IN p_out_FlujoTotalizadoFinal TABLE ( "TipoCartera"							NVARCHAR(40)
								 ,"FechaOperacion"	NVARCHAR(10)
								 ,"IdBoveda"			NVARCHAR(3)
								 ,"CantidadDisponible"	DECIMAL(26,8)
								 ,"IdInstrumento" 		BIGINT
								 ,"Nemotecnico"			NVARCHAR(40)
								 ,"IdEmisor"			NVARCHAR(40)
								 ,"IdTipoInstrumento"	NVARCHAR(3)
								 ,"NombreTipoInstrumento" NVARCHAR(100)
								 ,"IdClasificacionTipoInstrumento"	NVARCHAR(3)
								 ,"NombreClasificacionTipoInstrumento"	NVARCHAR(100)
								 ,"EsFungible"			NVARCHAR(1)
								 ,"FechaVencimiento" NVARCHAR(10)
								 ,"IdMoneda"	 	NVARCHAR(3)
								 ,"PrecioMercadoCustodia"			 DECIMAL(26,8)
								 ,"TasaMercadoActual"		 DECIMAL(26,8)
								 ,"IdCliente"		BIGINT
								 ,"IdPortafolio"	BIGINT
								 ,"ValorMercadoCustodia"					DECIMAL(26,8)
								 ,"MontoMercadoCustodia"					DECIMAL(26,8)
								    ),
    OUT p_Salida                TABLE(
    
    								"OrdenInterno"							BIGINT,     
									"CodigoPortafolio"						NVARCHAR(40), 
									"IdPortafolio"							BIGINT,
									"Grupo"									NVARCHAR(40),
									"IdClasificacionTipoInstrumento"		NVARCHAR(10),
									"Nemotecnico"							NVARCHAR(40),
									"IdMoneda"								NVARCHAR(3),
									"Cantidad"								DECIMAL(26,8),
									"ValorPresente"							DECIMAL(26,8),
									"FechaVencimiento"						NVARCHAR(10),
									"PrecioCierre"							DECIMAL(26,8),
									"Tasa"									DECIMAL(26,8),
									"IdCliente"								BIGINT,
									"PrecioDecimales"						DECIMAL(26,8),
									"TasaDecimales"							DECIMAL(26,8)
    							)
)


	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER 
	DEFAULT SCHEMA "BSCL"
    AS
BEGIN
		DECLARE v_FechaConsulta 				NVARCHAR(10) := '2300-12-31';
		DECLARE v_fechaHoraSistema              NVARCHAR(40);	
	
		v_FechaConsulta=p_FechaConsulta;
		v_fechaHoraSistema = p_fechaHoraSistema;	
	
		v_decimales = SELECT 
		     MF."PrecioDecimales"
		    ,MF."TasaDecimales" 
		    ,MF."IdMoneda"
		    FROM "BSCL.Plataforma.BD::PLA.MonedaFormato" MF 
		    WHERE  MF."IdTipoInstrumento" = 'SIM'
		    AND MF."IdUsoMonedaFormato" = 'GEN'
		    AND MF."IdTipoOperacion" ='';
		   
   
		IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN
				
			p_Salida = SELECT 
							    20 AS "OrdenInterno"
							   	,CP."CodigoPortafolio" as "CodigoPortafolio"
							    ,CP."IdPortafolio" as "IdPortafolio"
							    ,'OPERACIONES A PLAZO DE ' || CASE WHEN OPE."AccionOperacion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END AS "Grupo"
							    ,COALESCE(CAR."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
							    ,COALESCE(Sub."Nemotecnico",'') AS "Nemotecnico"
							    ,COALESCE(CAR."IdMonedaInstrumento" ,'') AS "IdMoneda"
							    ,CAR."INGCantidad" AS "Cantidad"
							    ,PREMER."PrecioCierre"*CAR."INGCantidad" AS "ValorPresente"
							    ,TO_VARCHAR (COALESCE(CAR."FechaVencimientoOperacion" ,''), 'DD/MM/YYYY')    AS "FechaVencimiento"
							    ,PREMER."PrecioCierre"         AS "PrecioCierre"
							    ,COALESCE(CAR."TasaCompra" ,0) AS "Tasa"
							    ,CAR."IdCliente"
							    ,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
							    ,COALESCE(MF."TasaDecimales" , 0) AS "TasaDecimales"
							FROM "BSCL.Custodia.BD::HIS.Cartera"  AS CAR
							INNER JOIN "BSCL.Custodia.BD.Vistas::HIS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = CAR."FolioCartera" AND ultSub."FechaRespaldo"=CAR."FechaRespaldo" 
							INNER JOIN "BSCL.Custodia.BD::HIS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" AND Sub."FechaRespaldo"=CAR."FechaRespaldo" )
							INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
							INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
							ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
							INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
							ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion" 
							LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaInstrumento" 
							LEFT JOIN (
									SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
									FROM ( 	
										SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
										,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
										FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
										INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
										ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
						 				where prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
									) PRE	
						WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
				
			) AS  PREMER
				ON (Sub."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa )
				
								
							WHERE CAR."IdTipoOperacion" IN ('COMSIM','VENSIM')
							AND CAR."IdEstado" IN ('VIG')
							AND CAR."IdTipoInstrumento" = 'SIM'
							AND TO_DATE(CAR."INGFechaOperacion") <=TO_DATE(:p_FechaConsulta)
                            AND  CAR."FechaRespaldo" = v_FechaConsulta
							AND CAR."IdCliente" IN (SELECT DISTINCT "IdCliente" FROM :p_cliente)
							AND CAR."IdPortafolio" IN (SELECT DISTINCT "IdPortafolio" FROM :p_cliente);
						
				
	    ELSE 
	    		p_Salida = SELECT 
					    20 AS "OrdenInterno"
					    ,CP."CodigoPortafolio" as "CodigoPortafolio"
					    ,CP."IdPortafolio" as "IdPortafolio"
					    ,'OPERACIONES A PLAZO DE ' || CASE WHEN OPE."AccionOperacion" = 'COMPRA' THEN 'VENTA' ELSE 'COMPRA' END AS "Grupo"
					    ,COALESCE(CAR."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
					    ,COALESCE(Sub."Nemotecnico",'') AS "Nemotecnico"
					    ,COALESCE(CAR."IdMonedaInstrumento" ,'') AS "IdMoneda"
					    ,CAR."INGCantidad" AS "Cantidad"
					    ,PREMER."PrecioCierre"*CAR."INGCantidad" AS "ValorPresente"
					    ,TO_VARCHAR (COALESCE(CAR."FechaVencimientoOperacion" ,''), 'DD/MM/YYYY')    AS "FechaVencimiento"
					    ,PREMER."PrecioCierre"         AS "PrecioCierre"
					    ,COALESCE(CAR."TasaCompra" ,0) AS "Tasa"
					    ,CAR."IdCliente"
					    ,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
						,COALESCE(MF."TasaDecimales" , 0) AS "TasaDecimales"
					FROM "BSCL.Custodia.BD::CUS.Cartera"  AS CAR
					INNER JOIN "BSCL.Custodia.BD.Vistas::CUS.ConsultarUltimoMovimientoSubyacente" as ultSub ON ultSub."FolioCarteraRelacionada" = CAR."FolioCartera"
 					INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" as Sub ON (Sub."IdSubyacente" = ultSub."IdSubyacente" )
					INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
					INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
					ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")
					INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
					ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion"
					LEFT JOIN :v_decimales AS MF ON MF."IdMoneda" = CAR."IdMonedaInstrumento"
					LEFT JOIN (
							SELECT PRE."IdInstrumento",PRE."PrecioCierre",PRE."Fecha",PRE."IdEmpresa"	
							FROM ( 	
								SELECT row_number() over (partition by prec."IdInstrumento",SIS."IdEmpresa" order by prec."IdInstrumento",prec."Fecha" desc) AS row_num
								,prec."IdInstrumento",prec."Fecha",	prec."IdBolsa",prec."PrecioCierre",SIS."IdEmpresa"
								FROM "BSCL.Plataforma.BD::INS.BolsaInstrumentoPrecio" prec
								INNER JOIN 	"BSCL.Plataforma.BD::PLA.Configuracion" SIS 
								ON  ( SIS."CodigoParametro" = 'PLA.FechaSistema') 
				 				where prec."IdBolsa" = 'BCS' AND TO_DATE(prec."Fecha") <= TO_DATE(v_FechaConsulta)
							) PRE	
				WHERE PRE.row_num = 1 AND PRE."PrecioCierre" > 0
			) AS  PREMER
				ON (Sub."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa )
					WHERE CAR."IdTipoOperacion" IN ('COMSIM','VENSIM')
					AND CAR."IdEstado" IN ('VIG')
					AND CAR."IdTipoInstrumento" = 'SIM'
					AND TO_DATE(CAR."INGFechaOperacion") <=TO_DATE(:p_FechaConsulta)
					AND CAR."IdCliente" IN (SELECT DISTINCT "IdCliente" FROM :p_cliente)
					AND CAR."IdPortafolio" IN (SELECT DISTINCT "IdPortafolio" FROM :p_cliente);
			
		END IF;
END;