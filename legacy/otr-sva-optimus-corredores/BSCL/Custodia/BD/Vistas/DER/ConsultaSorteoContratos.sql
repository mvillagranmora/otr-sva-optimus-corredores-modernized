SET SCHEMA "BSCL"; 
GO
DROP VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos";
GO
CREATE VIEW "BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos" 

AS
SELECT  
	 SOR."DocumentoIdentidad"
	,POR."IdPortafolio"
	,COALESCE(TO_VARCHAR(POR."CodigoPortafolio"),'0')				AS "PortafolioDerivados"
	,SOR."IdTipoOperacion" 				
	,COALESCE(INS."Nemotecnico",CONCAT('NO EXISTE:  ', SOR."Serie"))	AS "Serie"--	SOR."Serie"
	,SOR."EsPutEsCall" 					
	,SOR."CodigoCorredora" 				
	,COALESCE(GRU."GlosaCodigoClasificacion",'') 			AS "CodigoClasificacion" 			
	,COALESCE(TIP."Nombre",'')								AS "IdTipoInstrumento"	 			
	,COALESCE(LIQ."NombreParametro",'')						AS "ModalidadLiquidacion" 			
	,CASE INS."ModalidadEjercicio"				
		WHEN 'M' THEN 	'MANUAL'
		ELSE    		'AUTOMATICA' 
	END AS "ModalidadEjercicio"		 			
	,COALESCE(EST."NombreParametro",'')						AS "EstiloOpcion"		 					
	,SOR."Fecha" 						
	,SOR."FechaVencimiento" 				
	,SOR."Subyacente"					
	,SOR."Strike" 						
	,SOR."PrecioSubyacente" 				
	,SOR."PerdidaGanancia" 						
	,SOR."SaldoEjercicio"				
	,SOR."SaldoNeto" 					
	,SOR."SaldoPrima" 					
	,SOR."SaldoSorteo" 					
	,SOR."IdFondo" 						
	,SOR."IdFondoCliente"				
	,SOR."LadoDiaAnterior"				
	,SOR."LadoDia"						
	,SOR."LadoNeto"						
	,SOR."LadoTr"						
	,SOR."LotePadron" 					
	,SOR."ContratosAbiertosNeto" 		
	,SOR."ContratosAbiertosDiaAnterior"	
	,SOR."ContratosAbiertosDia" 			
	,SOR."ContratosEjercidos" 			
	,SOR."ContratosSorteados" 			
	,SOR."CantidadContratosTr"			
	,SOR."Observacion"
	,CASE WHEN SOR."Sorteado" IS NULL THEN 'NO' ELSE SOR."Sorteado" END AS "Sorteado"
	
FROM "BSCL.Custodia.BD::CUS.SorteoContratos"							AS SOR
LEFT JOIN "BSCL.Portafolio.BD.Vistas::ConsultarPortafolio"				AS POR
ON (SOR."PortafolioDerivados" = POR."IdPortafolio")
LEFT  JOIN "BSCL.Plataforma.BD.Vistas::INS.ConsultarInstrumento"		AS INS 
ON (INS."Nemotecnico" = SOR."Serie")
LEFT JOIN "BSCL.Plataforma.BD::INS.GrupoTipoInstrumento"	AS GRU 
ON (GRU."CodigoClasificacion" = CASE SOR."CodigoClasificacion" WHEN 'RV' THEN 'RENTAVARIABLE' ELSE SOR."CodigoClasificacion" END AND
	GRU."IdTipoInstrumento" = INS."IdTipoInstrumento" AND GRU."IdGrupoProducto" = 'OP')
	
	
LEFT JOIN "BSCL.Plataforma.BD::INS.TipoInstrumento"									AS TIP
ON (INS."IdTipoInstrumento" = TIP."IdTipoInstrumento" AND INS."IdTipoInstrumento" = GRU."IdTipoInstrumento")
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarModalidadLiquidacionOpcion" 		AS LIQ
ON (LIQ."ValorParametro" = INS."ModalidadLiquidacion"	AND LIQ."NombreTipoParametro" 	= 'ModalidadOpcion'	)
LEFT JOIN "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEstiloOpcion"					AS EST
ON (EST."ValorParametro" = INS."EstiloOpcion" AND EST."NombreTipoParametro" = 'EstiloOpcion')

 
WITH READ ONLY;


GO
GRANT SELECT ON "BSCL.Custodia.BD.Vistas::DER.ConsultaSorteoContratos" TO BCS_DESARROLLADOR WITH GRANT OPTION;
GO