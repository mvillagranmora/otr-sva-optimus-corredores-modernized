set schema "BSCL";

drop procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1823.ObtenerTotales";

create procedure "BSCL.Normativa.BD.Servicios::TRI.DJ1823.ObtenerTotales"
(
    ------------ Parametros de Entrada Comunes ----------

    -- ID empresa del login.
    in p_IdEmpresa bigint,

    -- ID usuario del login.
    in p_IdUsuario bigint,

    -- ID modulo invocador.
    in p_IdModulo nvarchar(3),

    ------------ Parametros de Entrada Propios ----------

    -- ID del documento.
    in p_IdDocumento bigint,

    -- Filtro VoBo del usuario.
    in p_VboUsuario nvarchar(3) default null,

    -- Filtro observaciones.
    in p_Observaciones nvarchar(252) default null,

    -- Filtro fuentes de datos.
    in p_FuenteDatos nvarchar(42) default null,

    in p_RutCliente nvarchar(42) default null,

    -- Rango del ID cliente.
    in p_IdCliente_Desde bigint default null,
    in p_IdCliente_Hasta bigint default null,

    -- Rango del ID portafolio.
    in p_IdPortafolio_Desde bigint default null,
    in p_IdPortafolio_Hasta bigint default null,

	in p_RutInstitucionReceptora nvarchar(42) default null,

    in p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Desde decimal(26,8) default null,
    in p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Hasta decimal(26,8) default null,

    in p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Desde decimal(26,8) default null,
    in p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Hasta decimal(26,8) default null,

    in p_MontoRetiroInversion57BIS_Desde decimal(26,8) default null,
    in p_MontoRetiroInversion57BIS_Hasta decimal(26,8) default null,
    
    in p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Desde decimal(26,8) default null,
    in p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Hasta decimal(26,8) default null,
    
    in p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Desde decimal(26,8) default null,
    in p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Hasta decimal(26,8) default null,
    
    in p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Desde decimal(26,8) default null,
    in p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Hasta decimal(26,8) default null,
    
    in p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Desde decimal(26,8) default null,
    in p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Hasta decimal(26,8) default null,
    
    in p_MontoInversionEjercicioDesde01Ene2015_Desde decimal(26,8) default null,
    in p_MontoInversionEjercicioDesde01Ene2015_Hasta decimal(26,8) default null,

	in p_MontoRetiroEjercicioDesde01Ene2015_Desde decimal(26,8) default null,
    in p_MontoRetiroEjercicioDesde01Ene2015_Hasta decimal(26,8) default null,
    
    in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Desde decimal(26,8) default null,
    in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Hasta decimal(26,8) default null,
    
    in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Desde decimal(26,8) default null,
    in p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Hasta decimal(26,8) default null,
    
    in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Desde decimal(26,8) default null,
    in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Hasta decimal(26,8) default null,
    
    in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Desde decimal(26,8) default null,
    in p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Hasta decimal(26,8) default null,
    
    in x_CodigoValidacion nvarchar(40) default null,

    ------------ Salidas ----------

    -- Tabla de totales.
    -- Debe incluir las columnas sumables de la tabla de detalle.
    out p_out_tbl table (
        "TYPE" nvarchar(100),
        "COUNT" bigint,
        "SaldoAhorroNetoHasta31Dic2014MontoPositivo" decimal(26,8),
        "SaldoAhorroNetoHasta31Dic2014MontoNegativo"   decimal(26,8),
        "MontoRetiroInversion57BIS" decimal(26,8),
        "SaldoAhorroNetoDesde01Ene2015MontoPositivo" decimal(26,8),
        "SaldoAhorroNetoDesde01Ene2015MontoNegativo" decimal(26,8),
        "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo" decimal(26,8),
        "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo" decimal(26,8),
        "MontoInversionEjercicioDesde01Ene2015" decimal(26,8),
        "MontoRetiroEjercicioDesde01Ene2015" decimal(26,8),
        "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo" decimal(26,8),
        "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo" decimal(26,8),
        "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo" decimal(26,8),
        "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo" decimal(26,8)
		)
	)
    language sqlscript
    sql security invoker
    default schema "BSCL"
as
begin
    ------------ Logica de Consulta ----------

	if x_CodigoValidacion is null then
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoPositivo") as "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoNegativo") as "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
	                    sum("MontoRetiroInversion57BIS") as "MontoRetiroInversion57BIS",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoPositivo") as "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoNegativo") as "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo") as "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo") as "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoInversionEjercicioDesde01Ene2015") as "MontoInversionEjercicioDesde01Ene2015",
	                    sum("MontoRetiroEjercicioDesde01Ene2015") as "MontoRetiroEjercicioDesde01Ene2015",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo"
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1823"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoPositivo") as "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoNegativo") as "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
	                    sum("MontoRetiroInversion57BIS") as "MontoRetiroInversion57BIS",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoPositivo") as "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoNegativo") as "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo") as "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo") as "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoInversionEjercicioDesde01Ene2015") as "MontoInversionEjercicioDesde01Ene2015",
	                    sum("MontoRetiroEjercicioDesde01Ene2015") as "MontoRetiroEjercicioDesde01Ene2015",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo"
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1823"
	                    where "IdDocumento" = p_IdDocumento
	                    and ("VboUsuario" like p_VboUsuario or p_VboUsuario is null)
	                    and ("Observaciones" like p_Observaciones or p_Observaciones is null)
	                    and ("FuenteDatos" like p_FuenteDatos or p_FuenteDatos is null)
	
	                    and ("RutCliente" like p_RutCliente or p_RutCliente is null)
	                    and ("IdCliente" >= p_IdCliente_Desde or p_IdCliente_Desde is null)
	                    and ("IdCliente" <= p_IdCliente_Hasta or p_IdCliente_Hasta is null)
	                    and ("IdPortafolio" >= p_IdPortafolio_Desde or p_IdPortafolio_Desde is null)
	                    and ("IdPortafolio" <= p_IdPortafolio_Hasta or p_IdPortafolio_Hasta is null)
	                    and ("SaldoAhorroNetoHasta31Dic2014MontoPositivo" >= p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Desde or p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Desde is null)
	                    and ("SaldoAhorroNetoHasta31Dic2014MontoPositivo" <= p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Hasta or p_SaldoAhorroNetoHasta31Dic2014MontoPositivo_Hasta is null)
	                    and ("SaldoAhorroNetoHasta31Dic2014MontoNegativo" >= p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Desde or p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Desde is null)
	                    and ("SaldoAhorroNetoHasta31Dic2014MontoNegativo" <= p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Hasta or p_SaldoAhorroNetoHasta31Dic2014MontoNegativo_Hasta is null)
	                    and ("MontoRetiroInversion57BIS" >= p_MontoRetiroInversion57BIS_Desde or p_MontoRetiroInversion57BIS_Desde is null)
	                    and ("MontoRetiroInversion57BIS" <= p_MontoRetiroInversion57BIS_Hasta or p_MontoRetiroInversion57BIS_Hasta is null)
	                    and ("SaldoAhorroNetoDesde01Ene2015MontoPositivo" >= p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Desde or p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Desde is null)
	                    and ("SaldoAhorroNetoDesde01Ene2015MontoPositivo" <= p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Hasta or p_SaldoAhorroNetoDesde01Ene2015MontoPositivo_Hasta is null)
	                    and ("SaldoAhorroNetoDesde01Ene2015MontoNegativo" >= p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Desde or p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Desde is null)
	                    and ("SaldoAhorroNetoDesde01Ene2015MontoNegativo" <= p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Hasta or p_SaldoAhorroNetoDesde01Ene2015MontoNegativo_Hasta is null)
	                    and ("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo" >= p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Desde or p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Desde is null)
	                    and ("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo" <= p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Hasta or p_SaldoArrastreEjercicioDesde01Ene2015MontoPositivo_Hasta is null)
	                    and ("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo" >= p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Desde or p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Desde is null)
	                    and ("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo" <= p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Hasta or p_SaldoArrastreEjercicioDesde01Ene2015MontoNegativo_Hasta is null)
	                    and ("MontoInversionEjercicioDesde01Ene2015" >= p_MontoInversionEjercicioDesde01Ene2015_Desde or p_MontoInversionEjercicioDesde01Ene2015_Desde is null)
	                    and ("MontoInversionEjercicioDesde01Ene2015" <= p_MontoInversionEjercicioDesde01Ene2015_Hasta or p_MontoInversionEjercicioDesde01Ene2015_Hasta is null)
	                    and ("MontoRetiroEjercicioDesde01Ene2015" >= p_MontoRetiroEjercicioDesde01Ene2015_Desde or p_MontoRetiroEjercicioDesde01Ene2015_Desde is null)
	                    and ("MontoRetiroEjercicioDesde01Ene2015" <= p_MontoRetiroEjercicioDesde01Ene2015_Hasta or p_MontoRetiroEjercicioDesde01Ene2015_Hasta is null)
	                    and ("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo" >= p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Desde or p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Desde is null)
	                    and ("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo" <= p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Hasta or p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo_Hasta is null)
	                    and ("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo" >= p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Desde or p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Desde is null)
	                    and ("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo" <= p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Hasta or p_MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo_Hasta is null)
	                    and ("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo" >= p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Desde or p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Desde is null)
	                    and ("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo" <= p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Hasta or p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo_Hasta is null)
	                    and ("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo" >= p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Desde or p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Desde is null)
	                    and ("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo" <= p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Hasta or p_MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo_Hasta is null);
      else
	    p_out_tbl = select
	                    'TOTAL' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoPositivo") as "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoNegativo") as "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
	                    sum("MontoRetiroInversion57BIS") as "MontoRetiroInversion57BIS",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoPositivo") as "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoNegativo") as "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo") as "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo") as "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoInversionEjercicioDesde01Ene2015") as "MontoInversionEjercicioDesde01Ene2015",
	                    sum("MontoRetiroEjercicioDesde01Ene2015") as "MontoRetiroEjercicioDesde01Ene2015",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo"
	                    
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1823"
	                    where "IdDocumento" = p_IdDocumento
	                union all
	                select
	                    'FILTRO' as "TYPE",
	                    count(*) as "COUNT",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoPositivo") as "SaldoAhorroNetoHasta31Dic2014MontoPositivo",
	                    sum("SaldoAhorroNetoHasta31Dic2014MontoNegativo") as "SaldoAhorroNetoHasta31Dic2014MontoNegativo",
	                    sum("MontoRetiroInversion57BIS") as "MontoRetiroInversion57BIS",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoPositivo") as "SaldoAhorroNetoDesde01Ene2015MontoPositivo",
	                    sum("SaldoAhorroNetoDesde01Ene2015MontoNegativo") as "SaldoAhorroNetoDesde01Ene2015MontoNegativo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoPositivo") as "SaldoArrastreEjercicioDesde01Ene2015MontoPositivo",
	                    sum("SaldoArrastreEjercicioDesde01Ene2015MontoNegativo") as "SaldoArrastreEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoInversionEjercicioDesde01Ene2015") as "MontoInversionEjercicioDesde01Ene2015",
	                    sum("MontoRetiroEjercicioDesde01Ene2015") as "MontoRetiroEjercicioDesde01Ene2015",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadFFMMEjercicioDesde01Ene2015MontoNegativo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoPositivo",
	                    sum("MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo") as "MontoRentabilidadOtrosInstrumentosEjercicioDesde01Ene2015MontoNegativo"
	                    
	
	                    from "BSCL.Normativa.BD::TRI.DatosDJ1823"
	                    where "IdDocumento" = p_IdDocumento
	                    and "ID" in (
	                        select a."IdDetalleDocumento"
	                        from "BSCL.Normativa.BD::TRI.DetalleValidacion" a
	                        left join "BSCL.Normativa.BD::TRI.ResumenValidacion" b
	                        on a."IdResumenValidacion"=b."ID"
	                        where b."CodigoValidacion"=x_CodigoValidacion
	                    )
	                ;
      end if;
  end;
