SET SCHEMA "BSCL";
GO
DROP FUNCTION "BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor";
GO

CREATE FUNCTION 
"BSCL.Contabilidad.BD.Utilitarios::ITZ.ObtenerTipoEmisor" (
	p_IdClasificacionTipoInstrumento 	NVARCHAR(3),
	p_IdTipoInstrumento					NVARCHAR(3),
	p_Nemotecnico						NVARCHAR(40),
	p_IdEmisor							NVARCHAR(40)
	

) 
RETURNS p_out_TipoEmisor NVARCHAR(2) 
LANGUAGE SQLSCRIPT
SQL SECURITY INVOKER 
AS

BEGIN
	DECLARE v_TipoEmisor NVARCHAR(2);
	v_TipoEmisor := '';
  
	IF COALESCE(:p_IdClasificacionTipoInstrumento,'') = 'RF'  THEN
		p_out_TipoEmisor :=	CASE
								WHEN p_IdTipoInstrumento = 'BCD' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'BCP' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'BCU' THEN 'ES'
								WHEN p_IdTipoInstrumento = 'BTP' THEN 'ES'
								WHEN p_IdTipoInstrumento = 'BTU' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'BCX' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'ZER' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'CER' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'XER' THEN 'ES'  
								WHEN p_IdTipoInstrumento = 'PRC' THEN 'ES'
								WHEN p_IdTipoInstrumento = 'PCX' THEN 'ES'
								WHEN p_IdTipoInstrumento = 'PRD' THEN 'ES'
								WHEN p_IdTipoInstrumento = 'BBC' THEN 'EF'
								WHEN p_IdTipoInstrumento = 'LTH' THEN 'EF'
								WHEN p_IdTipoInstrumento = 'BCA' THEN 'OE'
								WHEN p_IdTipoInstrumento = 'BR'  THEN 'OE'
								WHEN p_IdTipoInstrumento = 'BH'  THEN 'OE'
								WHEN p_IdTipoInstrumento = 'COR' THEN 'OE'
								WHEN p_IdTipoInstrumento = 'BUS' THEN 'OE'
								WHEN p_IdTipoInstrumento = 'BEM' THEN 'OE'
								WHEN p_IdTipoInstrumento = 'BVL' THEN 'OE'
								WHEN p_IdTipoInstrumento = 'ORF' THEN 'OE'								
								ELSE ''
								
						END;
	END IF;
	
	IF COALESCE(:p_IdClasificacionTipoInstrumento,'') = 'IF'  THEN
		p_out_TipoEmisor :=	CASE
								WHEN p_IdTipoInstrumento = 'PAG' THEN								
									CASE 
										WHEN LEFT(p_Nemotecnico,4) = 'PDBC' THEN 'ES' 
										WHEN LEFT(p_Nemotecnico,4) = 'PRBC' THEN 'ES' 
									 	WHEN LEFT(p_Nemotecnico,3) = 'PRT'  THEN 'ES'  
										WHEN LEFT(p_Nemotecnico,3) = 'PDT'  THEN 'ES'
										WHEN LEFT(p_Nemotecnico,6) = 'PAGARE' AND p_IdEmisor = 'CENTRAL' THEN 'ES'  										
										ELSE 'OE'
									END

		
								ELSE ''
								
						END;
	END IF;
	
	p_out_TipoEmisor := IFNULL(p_out_TipoEmisor,'21');  
END
