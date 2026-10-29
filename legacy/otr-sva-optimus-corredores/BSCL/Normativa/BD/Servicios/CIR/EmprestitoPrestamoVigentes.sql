SET SCHEMA "BSCL";

DROP PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.EmprestitoPrestamoVigentes";

CREATE PROCEDURE "BSCL.Normativa.BD.Servicios::CIR.EmprestitoPrestamoVigentes" ( 
    IN p_IdEmpresa				BIGINT,
    IN p_FechaConsulta			NVARCHAR(10),
    IN p_OrdenInterno           BIGINT,
    IN p_Grupo                  NVARCHAR(60),
    IN p_IdTipoOperacion        NVARCHAR(10),
    IN p_EsVentaCorta			NVARCHAR(1),
    IN p_IdTipoPrestamo         NVARCHAR(10),
    IN p_cliente                TABLE("IdCliente" BIGINT,"IdPortafolio" BIGINT),
    IN p_fechaHoraSistema       NVARCHAR(40),
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
	
		
   
		IF  TO_VARCHAR(v_FechaConsulta, 'YYYY-MM') < TO_VARCHAR(v_fechaHoraSistema, 'YYYY-MM') THEN				
			p_Salida = SELECT 
			 		:p_OrdenInterno AS "OrdenInterno"
					,CP."CodigoPortafolio" as "CodigoPortafolio"
					,CP."IdPortafolio" as "IdPortafolio"
					,:p_Grupo AS "Grupo"
					,COALESCE(CAR."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
					,COALESCE(SUB."Nemotecnico",'') AS "Nemotecnico"
					,COALESCE(CAR."IdMonedaInstrumento",'') AS "IdMoneda"  
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN CAR."Cantidad" ELSE  (CAR."Cantidad" * -1) END  AS "Cantidad"
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN CAR."INGMontoATasaCompraActual" ELSE (CAR."INGMontoATasaCompraActual"*-1) END AS "ValorPresente"
					,TO_VARCHAR (COALESCE(CAR."FechaVencimientoOperacion",''), 'DD/MM/YYYY')		AS "FechaVencimiento"
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN PREMER."PrecioCierre" ELSE (PREMER."PrecioCierre" * -1 ) END       AS "PrecioCierre"
					,COALESCE(MOVCOM."Tasa",0) AS "Tasa"
					,CAR."IdCliente"
					,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
					,COALESCE(MF."TasaDecimales" , 0)  AS "TasaDecimales"
			 FROM "BSCL.Custodia.BD::HIS.Cartera" CAR
			 INNER JOIN "BSCL.Custodia.BD::CUS.Cartera" CARNOHIS
			 ON CAR."IdCartera" = CARNOHIS."IdCartera"
			 INNER JOIN  "BSCL.Facturacion.BD::FAC.Comprobante" COMP 
			 ON COMP."IdComprobante" = CAR."FolioComprobante" 
			 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVCOM
             ON MOVCOM."IdComprobante" = COMP."IdComprobante"
			 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")	
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
			ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion" 
			INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB
				ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera" 
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
		     ON MF."IdTipoInstrumento" = CAR."IdTipoInstrumento"
				AND MF."IdUsoMonedaFormato" = 'GEN'
				AND MF."IdMoneda" = MF."IdMoneda" 
				AND MF."IdTipoOperacion" = ''
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
				ON (SUB."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa )
			WHERE  CAR."IdEstado" IN ('VIG')
			AND TO_DATE(CAR."INGFechaOperacion") <=TO_DATE(:p_FechaConsulta)
			AND  CAR."FechaRespaldo" = v_FechaConsulta
			AND CAR."IdCliente"IN (SELECT DISTINCT "IdCliente" FROM :p_cliente)
			AND MOVCOM."EsVentaCorta" = :p_EsVentaCorta
			AND CAR."IdEmpresa" = :p_IdEmpresa
			AND CARNOHIS."IdTipoPrestamo" = :p_IdTipoPrestamo
			AND CAR."IdTipoOperacion" = :p_IdTipoOperacion;
			
	    ELSE 
				p_Salida = SELECT 
			 		:p_OrdenInterno AS "OrdenInterno"
					,CP."CodigoPortafolio" as "CodigoPortafolio"
					,CP."IdPortafolio" as "IdPortafolio"
					,:p_Grupo AS "Grupo"
					,COALESCE(CAR."IdClasificacionTipoInstrumento" ,'') AS "IdClasificacionTipoInstrumento"
					,COALESCE(SUB."Nemotecnico",'') AS "Nemotecnico"
					,COALESCE(CAR."IdMonedaInstrumento",'') AS "IdMoneda"  
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN CAR."Cantidad" ELSE  (CAR."Cantidad" * -1) END  AS "Cantidad"
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN CAR."INGMontoATasaCompraActual" ELSE (CAR."INGMontoATasaCompraActual"*-1) END AS "ValorPresente"
					,TO_VARCHAR (COALESCE(CAR."FechaVencimientoOperacion",''), 'DD/MM/YYYY')		AS "FechaVencimiento"
					,CASE :p_IdTipoOperacion WHEN 'EMPRES' THEN PREMER."PrecioCierre" ELSE (PREMER."PrecioCierre" * -1 ) END       AS "PrecioCierre"
					,COALESCE(MOVCOM."Tasa",0) AS "Tasa"
					,CAR."IdCliente"
					,COALESCE(MF."PrecioDecimales", 0) AS "PrecioDecimales"
					,COALESCE(MF."TasaDecimales" , 0)  AS "TasaDecimales"
			 FROM "BSCL.Custodia.BD::CUS.Cartera" CAR
			 INNER JOIN  "BSCL.Facturacion.BD::FAC.Comprobante" COMP 
			 ON COMP."IdComprobante" = CAR."FolioComprobante" 
			 INNER JOIN "BSCL.Facturacion.BD::FAC.MovimientoComprobante" MOVCOM
             ON MOVCOM."IdComprobante" = COMP."IdComprobante"
			 INNER JOIN "BSCL.Portafolio.BD::POR.Portafolio" CP ON CP."IdPortafolio"=CAR."IdPortafolio"
			INNER JOIN "BSCL.Plataforma.BD::INS.ClasificacionTipoInstrumento" CLA
			ON (CLA."IdClasificacionTipoInstrumento"=CAR."IdClasificacionTipoInstrumento")	
			INNER JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" OPE
			ON CAR."IdTipoOperacion" = OPE."IdTipoOperacion" 
			INNER JOIN "BSCL.Custodia.BD::CUS.Subyacente" SUB
			ON SUB."FolioCarteraRelacionada" = CAR."FolioCartera"
			LEFT JOIN "BSCL.Plataforma.BD::PLA.MonedaFormato" MF
		     ON MF."IdTipoInstrumento" = CAR."IdTipoInstrumento"
				AND MF."IdUsoMonedaFormato" = 'GEN'
				AND MF."IdMoneda" = MF."IdMoneda" 
				AND MF."IdTipoOperacion" = ''
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
				ON (SUB."IdInstrumento"=PREMER."IdInstrumento"  AND  PREMER."IdEmpresa"=:p_IdEmpresa ) 
			WHERE  CAR."IdEstado" IN ('VIG')
			AND TO_DATE(CAR."INGFechaOperacion") <=TO_DATE(:p_FechaConsulta)
			AND CAR."IdCliente"IN (SELECT DISTINCT "IdCliente" FROM :p_cliente)
			AND MOVCOM."EsVentaCorta" =:p_EsVentaCorta
			AND CAR."IdEmpresa" = :p_IdEmpresa
			AND CAR."IdTipoPrestamo" = :p_IdTipoPrestamo
			AND CAR."IdTipoOperacion" = :p_IdTipoOperacion;		
		END IF;
END;