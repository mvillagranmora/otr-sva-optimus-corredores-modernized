SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Normativa.BD.Utilitarios::NOR.SinAcento";

CREATE FUNCTION "BSCL.Normativa.BD.Utilitarios::NOR.SinAcento" (
	p_Texto_UTF		NVARCHAR(250)
) 
	RETURNS p_out_ASCII NVARCHAR(250) 
	LANGUAGE SQLSCRIPT
	SQL SECURITY INVOKER AS
BEGIN
	BEGIN
		p_out_ASCII := REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( REPLACE(
			   		   REPLACE( REPLACE( REPLACE( REPLACE( REPLACE( 
						p_Texto_UTF,	'Ñ', 'N' ), 
										'Á', 'A' ),
										'É', 'E' ),
										'Í', 'I' ),
										'Ó', 'O' ),																														
										'Ú', 'U' ),
										'á', 'a' ),
										'é', 'e' ),
										'í', 'i' ),
										'ó', 'o' ),																														
										'ú', 'u' );
										
	END;
END;

