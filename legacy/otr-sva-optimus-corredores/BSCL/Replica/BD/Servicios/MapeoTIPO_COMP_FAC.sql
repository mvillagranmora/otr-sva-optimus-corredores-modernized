SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC";

CREATE FUNCTION "BSCL.Replica.BD.Servicios::MapeoTIPO_COMP_FAC"(IdTipoComprobante NVARCHAR(3))
	RETURNS result VARCHAR(11)
	LANGUAGE SQLSCRIPT READS SQL DATA
	AS
BEGIN 

    result =  '';
    IF IdTipoComprobante = 'FAC' THEN
    	result = 'FA';
	ELSEIF IdTipoComprobante = 'FAE' THEN
    	result = 'FD';
    ELSEIF IdTipoComprobante = 'FOC' THEN
    	result = 'FO';
   	ELSEIF IdTipoComprobante = 'FOE' THEN
    	result = 'FT';
    ELSEIF IdTipoComprobante = 'FEE' THEN
    	result = 'FX';
    ELSEIF IdTipoComprobante = 'FOX' THEN
    	result = 'FX';
    ELSEIF IdTipoComprobante = 'NCR' THEN
    	result = 'NC';
    ELSEIF IdTipoComprobante = 'NCE' THEN
    	result = 'ND';
    ELSEIF IdTipoComprobante = 'NDR' THEN
    	result = 'NB';
    ELSEIF IdTipoComprobante = 'NEO' THEN
    	result = 'NT';
   	ELSEIF IdTipoComprobante = 'NEE' THEN
    	result = 'NT';
	ELSE
		result = 'FD';
    END IF;

END;
