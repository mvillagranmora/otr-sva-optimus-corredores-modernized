SET SCHEMA "BSCL"; 
DROP VIEW "BSCL.Replica.BD.Vistas::Sebra.SUCURSAL"; 

CREATE VIEW "BSCL.Replica.BD.Vistas::Sebra.SUCURSAL" AS

	Select SUC."IdSucursal",
            COALESCE (CAST(SUC."CodigoSucursal" AS VARCHAR(4)),'')  AS "SUC_ID", -- CHAR(4)
            COALESCE ( 0 , 0 )  AS "FIL_ID", -- INT
            COALESCE ( REG."Codigo" , 0 )  AS "COM_ID", -- INT
            COALESCE (CAST(SUC."NombreSucursal" AS VARCHAR(30)),'')  AS "SUC_NOMBRE", -- VARCHAR(30)
            COALESCE (CAST('DESCONOCID' AS VARCHAR(10)),'')  AS "PAR_SUC_ZONA", -- VARCHAR(10)
            COALESCE (CAST(SUC."Calle" || ' ' || SUC."Numero" AS VARCHAR(50)),'')  AS "SUC_DIRECCION", -- VARCHAR(50)
            COALESCE (CAST(SUC."Telefono" AS VARCHAR(20)),'')  AS "SUC_TELEFONO", -- VARCHAR(20)
            COALESCE (CAST('' AS VARCHAR(20)),'')  AS "SUC_FAX", -- VARCHAR(20)
            COALESCE (CAST(CASE WHEN SUC."EstadoSucursal" = 'VIG' THEN 'S' ELSE 'N' END AS VARCHAR(1)),'')  AS "SUC_IND_HABILITADO" -- CHAR(1)

        FROM "BSCL.Plataforma.BD.Vistas::PLA.ConsultarEmpresa" EMP
        INNER JOIN "BSCL.Plataforma.BD::PLA.Sucursal" SUC ON EMP."IdEmpresa" = SUC."IdEmpresa"
		LEFT JOIN "BSCL.Plataforma.BD::REG.DivisionPais" REG ON REG."IdDivisionPais" = SUC."IdDivisionPais"
        WHERE EMP."IdEstado" = 'ACT' and SUC."CodigoSucursal" != ''


WITH READ ONLY;

GRANT SELECT ON "BSCL.Replica.BD.Vistas::Sebra.SUCURSAL" TO  BCS_DESARROLLADOR WITH GRANT OPTION;
