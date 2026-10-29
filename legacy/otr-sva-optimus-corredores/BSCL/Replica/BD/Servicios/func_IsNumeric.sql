SET SCHEMA "BSCL";

DROP FUNCTION "BSCL.Replica.BD.Servicios::func_IsNumeric";

CREATE FUNCTION "BSCL.Replica.BD.Servicios::func_IsNumeric"(myVarChar VARCHAR(100))
	returns result int
	LANGUAGE SQLSCRIPT READS SQL DATA
	AS 
begin
	declare aux double;
	
	DECLARE EXIT HANDLER FOR SQLEXCEPTION
		BEGIN
		 result := 0;
		END;
	
	 
	 aux := cast(trim(myVarChar) AS double );
	 IF (:aux IS NOT null) THEN 
	 	result :=1;
	 ELSE 
	    result := 0;
	 END IF;
	
	 

END;






