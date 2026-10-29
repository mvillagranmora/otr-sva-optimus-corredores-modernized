SELECT  OP."IdOperacion",
            OP."FolioOperacion",
            CAST (OP."FechaOperacion" AS DATE) AS "FechaOperacion",
            OP."IdTipoProducto",
            OP."IdTipoOperacion",
            T_Ope."Nombre" AS "NombreOperacion",
            OP."IdOperacionRelacionada",
            OP."Intermesa",
            OP."IdMesaOrigen",
            mesOri."Nombre" AS "NombreMesaOrigen",
            OP."IdMesaDestino",
            mesDes."Nombre" AS "NombreMesaDestino",
            OP."IdCliente",
            OP."IdPortafolio",
            OP."IdClienteCorredor",
            OP."IdPortafolioCorredor",
            CASE WHEN CLI."Tipo"='J' THEN CLI."RazonSocial" ELSE CLI."NombreCompleto" END AS "RazonSocial",
            OP."MontoOperacion",
            OP."MontoOperacionInicial",
            OP."MontoOperacionFinal",
            OP."IdTipoReajuste",
            OP."IdMoneda",
            OP."Tasa",
            OP."MontoOperacionMoneda",
            OP."MontoOperacionInicialMoneda",
            OP."MontoOperacionFinalMoneda",
            OP."IdMonedaLiquidacion",
            OP."PlazoEnDias",
            CAST (OP."FechaVencimiento" AS DATE) AS "FechaVencimiento",            
            CAST (OP."FechaLiquidacion" AS DATE) AS "FechaLiquidacion", 
            OP."IdCondicionLiquidacion",
            CAST (OP."FechaCondicionLiquidacion" AS DATE) AS "FechaCondicionLiquidacion",
            OP."IdAgente",
            OP."TasaAgente",
            OP."IdEstado",
            Est_PLA."Nombre" AS "NombreEstado",
            CAST (OP."FechaCreacion" AS DATE) AS "FechaCreacion",
            OP."IdUsuarioCreacion",
           -- USR."CodigoUsuario",
            OP."IdUsuarioEdicion",
            usuSup."IdUsuario" AS "IdUsuarioSuperior",
            OP."Glosa",
            CLI."IdEmpresa",
            CLI."DocumentoIdentidad",
            OP."IdTipoCanalIngreso",
            OP."AfectaCustodia"
            ,OP."MontoOperacionAnticipo"
            ,OP."MontoOperacionInicialAnticipo"
            ,OP."MontoOperacionFinalAnticipo"
            ,OP."TasaAnticipo"
            ,OP."MontoOperacionMonedaAnticipo"
            ,OP."MontoOperacionInicialMonedaAnticipo"
            ,OP."MontoOperacionFinalMonedaAnticipo"
            ,OP."IdMonedaLiquidacionAnticipo"
            ,OP."IdCondicionLiquidacionAnticipo"
            ,CAST (OP."FechaCondicionLiquidacionAnticipo" AS DATE ) AS "FechaCondicionLiquidacionAnticipo"
    FROM    "BSCL.Mesa.BD::MES.Operacion" OP
            LEFT JOIN "BSCL.SocioNegocio.BD.Vistas::CLI.ConsultarCliente" CLI
            ON OP."IdCliente" = CLI."IdCliente" 
            LEFT JOIN "BSCL.Plataforma.BD::INS.TipoOperacion" T_Ope
            ON OP."IdTipoProducto" = T_Ope."IdTipoProducto" AND OP."IdTipoOperacion" = T_Ope."IdTipoOperacion"
            LEFT JOIN "BSCL.Mesa.BD::MES.EstadoOperacion" Est_MRF
            ON  OP."IdTipoProducto" = Est_MRF."IdTipoProducto" AND OP."IdEstado" = Est_MRF."IdEstado" AND Est_MRF."NombreEntidad" = 'INGRESO'
            LEFT JOIN "BSCL.Plataforma.BD::PLA.Estado" Est_PLA
            ON  Est_MRF."IdEstado" = Est_PLA."IdEstado"
            LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" mesOri ON (OP."IdMesaOrigen"  = mesOri."IdMesa")
            LEFT JOIN "BSCL.Mesa.BD::MES.Mesa" mesDes ON (OP."IdMesaDestino" = mesDes."IdMesa")
            INNER JOIN "BSCL.Plataforma.BD::ACC.Usuario" usu ON (OP."IdUsuarioCreacion" = usu."IdUsuario" AND usu."TipoEntidad" = 'EMP')
            INNER JOIN "BSCL.SocioNegocio.BD::EMP.Empleado" emp ON (usu."IdEntidad" = emp."IdEmpleado")
            LEFT JOIN "BSCL.Plataforma.BD::ACC.Usuario" usuSup ON (usuSup."IdEntidad" = emp."IdEmpleadoSupervisor")
	INNER JOIN "BSCL.Mesa.BD::GBL.Operacion" AS TMP ON (TMP."IdOperacion" = OP."IdOperacion");
     