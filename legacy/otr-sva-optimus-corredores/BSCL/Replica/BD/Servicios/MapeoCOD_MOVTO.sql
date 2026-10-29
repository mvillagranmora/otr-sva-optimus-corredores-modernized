SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Replica.BD.Servicios::MapeoCOD_MOVTO";

CREATE FUNCTION "BSCL.Replica.BD.Servicios::MapeoCOD_MOVTO"(IdTipoOperacion NVARCHAR(6),IdTipoProducto NVARCHAR(4),EsAPlazo NVARCHAR(1) DEFAULT 'N',IdFormaCustodia NVARCHAR(3) DEFAULT '')
	RETURNS result VARCHAR(11)
	LANGUAGE SQLSCRIPT READS SQL DATA
	AS
BEGIN 

    result =  '';
    IF IdTipoOperacion = 'COMACN' OR IdTipoOperacion = 'COMCFI' OR IdTipoOperacion = 'COMETN' 
    	OR IdTipoOperacion = 'COMACX' OR IdTipoOperacion = 'COMCFX' OR IdTipoOperacion = 'COMETX' OR IdTipoOperacion = 'COMOSA' THEN
    	result = 'CIRV';
	ELSEIF IdTipoOperacion = 'VENACN' OR IdTipoOperacion = 'VENCFI' OR IdTipoOperacion = 'VENETN' 
    	OR IdTipoOperacion = 'VENACX' OR IdTipoOperacion = 'VENCFX' OR IdTipoOperacion = 'VENETX' OR IdTipoOperacion = 'VENOSA'THEN
    	result = 'VIRV';
    ELSEIF IdTipoOperacion = 'ANCACN' OR IdTipoOperacion = 'ANCCFI' OR IdTipoOperacion = 'ANCETN' 
    	OR IdTipoOperacion = 'ANCACX' OR IdTipoOperacion = 'ANCCFX' OR IdTipoOperacion = 'ANCETX' OR IdTipoOperacion = 'ANCOSA' THEN
    	result = 'NRVC';
    ELSEIF IdTipoOperacion = 'ANVACN' OR IdTipoOperacion = 'ANVCFI' OR IdTipoOperacion = 'ANVETN' 
    	OR IdTipoOperacion = 'ANVACX' OR IdTipoOperacion = 'ANVCFX' OR IdTipoOperacion = 'ANVETX' OR IdTipoOperacion = 'ANVOSA'THEN
    	result = 'NRVV';
	ELSEIF IdTipoOperacion = 'COMMON' THEN
    	result = 'COMO';
    ELSEIF IdTipoOperacion = 'VENMON' THEN
    	result = 'VIMO';  
    ELSEIF IdTipoOperacion = 'COMRF' THEN
    	result = 'CIRF';
    ELSEIF IdTipoOperacion = 'ANCRF' THEN
    	result = 'NRFC';
    ELSEIF IdTipoOperacion = 'VENRF' THEN
    	result = 'VIRF';  
    ELSEIF IdTipoOperacion = 'ANVRF' THEN
    	result = 'NRFV'; 
    ELSEIF IdTipoOperacion = 'COMIIF' THEN
    	result = 'CIIF';
    ELSEIF IdTipoOperacion = 'ANCIIF' THEN
    	result = 'NIFC';  
    ELSEIF IdTipoOperacion = 'VENIIF' THEN
    	result = 'VIIF';
    ELSEIF IdTipoOperacion = 'ANVIIF' THEN
    	result = 'NIFV';
	ELSEIF IdTipoOperacion = 'VENSPT' THEN    	
		result = 'VFRD';
	ELSEIF IdTipoOperacion = 'COMSPT' THEN    	
		result = 'CFRD';
	ELSEIF IdTipoOperacion = 'COMSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'N' THEN    	
		result = 'CIRV';
	ELSEIF IdTipoOperacion = 'COMSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'S' THEN    	
		result = 'CITP';
	ELSEIF IdTipoOperacion = 'VENSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'N' THEN
		result = 'VIRV';
	ELSEIF IdTipoOperacion = 'VENSIM' AND IdTipoProducto = 'INSI' AND EsAPlazo = 'S' THEN    	
		result = 'VITP';
	ELSEIF IdTipoOperacion = 'COMOTC' THEN    	
		result = 'FCOT';
	ELSEIF IdTipoOperacion = 'VENOTC' THEN    	
		result = 'FVOT';
	ELSEIF IdTipoOperacion = 'ANVRF' THEN    	
		result = 'NRFV';
	ELSEIF IdTipoOperacion = 'ANVSIM' OR IdTipoOperacion = 'ANSIMV' AND (IdFormaCustodia = '' OR IdFormaCustodia = 'CUS') THEN    	
		result = 'NCTP';
	ELSEIF IdTipoOperacion = 'ANVSIM' OR IdTipoOperacion = 'ANSIMV' AND (IdFormaCustodia = 'NOM') THEN    	
		result = 'MCTP';
	ELSEIF IdTipoOperacion = 'ANCSIM' OR IdTipoOperacion = 'ANSIMC' AND (IdFormaCustodia = '' OR IdFormaCustodia = 'CUS') THEN    	
		result = 'NVTP';
	ELSEIF IdTipoOperacion = 'ANCSIM' OR IdTipoOperacion = 'ANSIMC' AND (IdFormaCustodia = 'NOM') THEN    	
		result = 'MVTP';
	ELSEIF IdTipoOperacion = 'ANCSPT' THEN
		result = 'NCOT';
	ELSEIF IdTipoOperacion = 'ANVSPT' THEN
		result = 'NVOT';
	ELSEIF IdTipoOperacion = 'ANCOTC' THEN
		result = 'NCOT';
	ELSEIF IdTipoOperacion = 'ANVOTC' THEN
		result = 'NVOT';
	ELSEIF IdTipoOperacion = 'SUSOSA' THEN
		result = 'CIRV';
	ELSE
		result = 'XXXX';
    END IF;

END;
