define ['react'], (React) ->

    { div, h2, button, span, input, label } = React.DOM
    { table, tr, th, td, tbody } = React.DOM
    
    BillOfMaterialsEntry = React.createClass
        getInitialState: ->
            {
                jitaPrice: NaN
                jitaSplit: NaN
                subMaterials: []
                subMaterialProduced: 0
                keyfix: Math.random()
                materialEfficiency: 0
            }

        getDefaultProps: ->
            {
                manufacturingProduces: 1
            }

        getSubMaterialsClick: ->
            bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct @props.materialID
            $.ajax bomURL
            .done ((result) ->
                # Assuming any 200 is success
                # alert result
                @setState {
                    subMaterials: result.materials
                    subMaterialProduced: result.quantityProduced
                }
            ).bind(this)
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                alert "Error thrown: " + errorThrown
            )
            
        addMfgCosts: (a,b) ->
            r = @refs.mfg.addCost b
            
        updateMaterialEfficiency: (value) ->
            @refs.mfg.clearCost()
            @setState { materialEfficiency: value }
            
        render: ->
            bomeFactory = React.createFactory BillOfMaterialsEntry
            bommFactory = React.createFactory BillOfMaterialMfgPrice
            meFactory = React.createFactory MaterialEfficiencyInput
            ac = @addMfgCosts
            rowClass = ''
            if (@props.materialBlueprint)
                rowClass = 'buildable-material-item'
            else
                rowClass = 'raw-material-item'
                
            getSubMaterials = null
            if (@props.materialBlueprint)
                getSubMaterials = td { key:@state.keyfix + '-subm-srch' }, [
                    button { 
                        key:@state.keyfix + '-subm-btn'
                        type: 'submit'
                        ref: 'itemSearchButton' 
                        className: 'btn btn-warning btn-sm'
                        onClick: @getSubMaterialsClick
                    }, [
                        span { key:@state.keyfix + '-subm-icn', className: "glyphicon glyphicon-wrench" }, null
                    ]
                    
                ]
                materialEfficiency = div { 
                    className: 'input-group input-group-sm'
                    style: {
                        width: '2.5em'
                    }
                    key: 'med'
                }, [
                    meFactory { 
                        key: 'me'
                        efficiency: @state.materialEfficiency
                        onChange: @updateMaterialEfficiency
                    }, null
                ]
            else
                if (@props.isChild)
                    rowClass = 'child-material-item'
                getSubMaterials = td { key:@state.keyfix + '-subm' }, null
                materialEfficiency = ""
                
            q = @props.quantity 
            me = Math.pow(0.99, @state.materialEfficiency)
            smp = @state.subMaterialProduced
            if (@state.subMaterials.length > 0)
                subMaterials = @state.subMaterials.map (row) ->
                    row.addCosts = ac
                    row.isChild = true
                    row.quantityModifier = (q * me)
                    row.key = row.materialID
                    row.manufacturingProduces = smp
                    bomeFactory row, null
            else
                subMaterials = null
                
            displayableQuantity = @props.quantity / @props.manufacturingProduces
            if (@props.quantityModifier)
                displayableQuantity = Math.round(displayableQuantity * @props.quantityModifier * 10, -1) / 10
            
                
            actualChildren = [
                td { key: 'q', style: { textAlign: 'right' } }, displayableQuantity
                td { key: 'n' }, @props.name
                td { key: 'v', style: { textAlign: 'right' } }, @props.volume
                td { key: 'matl', style: { textAlign: 'center' } }, @props.materialID
                td { key: 'jsl', style: { textAlign: 'right' } }, @state.jitaPrice.toLocaleString()
                td { key: 'jspl', style: { textAlign: 'right' } }, @state.jitaSplit.toLocaleString()
                getSubMaterials
                td { key: 'me'}, materialEfficiency
                bommFactory { key: 'mfg', ref: 'mfg' }, null
            ]
            
            actualEntry = tr { key: 'bome-ae', className: rowClass }, actualChildren
                
            if (@props.isChild)
                retVal = actualEntry
            else
                result = [actualEntry].concat(subMaterials)
                retVal = tbody { key: 'bomerow' }, result
                
            retVal
            
        getJitaPricing: ->
            jitaPriceURL = jsRoutes.controllers.MarketController.jitaPriceForItem(@props.materialID)
            ac = @props.addCosts
            $.ajax jitaPriceURL
            .done ((result) ->
                split = Math.ceil((result.buyPrice + result.sellPrice) / 2)
                
                displayableQuantity = @props.quantity
                
                if (@props.quantityModifier)
                    displayableQuantity = Math.round(100 * displayableQuantity * @props.quantityModifier / @props.manufacturingProduces) / 100
                
                cost = result.sellPrice * displayableQuantity
                splitCost = split * displayableQuantity
                this.setState { jitaPrice: cost, jitaSplit: splitCost }
                ac(cost, splitCost)
                
            ).bind(this)
            .fail((jqXHR, textStatus, errorThrown) ->
                this.setState { jitaPrice: 'n/a', jitaSplit: 'n/a'}
            )
            
        componentDidMount: ->
            @getJitaPricing()
            true
            
        componentWillReceiveProps: (nextProps) ->
            @setState {
                jitaPrice: NaN
                jitaSplit: NaN
                subMaterials: []
            }
            @getJitaPricing()
            true
            
    BillOfMaterialMfgPrice = React.createClass
        getInitialState: ->
            {
                amount: NaN
            }
            
        addCost: (cost) ->
            amt = @state.amount
            if (isNaN(amt))
                amt = cost
            else
                amt += cost
            
            @setState { amount: amt }
            
        clearCost: ->
            @setState { amount: 0 }
            
        render: ->
            if (isNaN @state.amount)
                td { key: 'mfgtd' }, null
            else
                td { key: 'mfgtd' }, @state.amount.toLocaleString()
                
    MaterialEfficiencyInput = React.createClass
        efficiencyChanged: (event) ->
            newValue = event.target.value  
            @props.onChange newValue
            
        render: ->
            input { 
                type: 'text'
                className: 'form-control'
                placeholder: '00'
                'aria-describedby': @props.key + 'lbl'
                style: {
                    borderRadius: '4px'
                }
                maxLength: 2
                onChange: @efficiencyChanged
                value: @props.efficiency
            }, null
            
    BillOfMaterialsFooter = React.createClass
        getInitialState: -> 
            {
                totalJita: 0
                totalJitaSplit: 0
            }
            
        addCosts: (jitaCost, jitaSplitCost) ->
            jitaTotal = @state.totalJita + jitaCost
            jitaSplitTotal = @state.totalJitaSplit + jitaSplitCost
            @setState {
                totalJita: jitaTotal
                totalJitaSplit: jitaSplitTotal
            }
            
        clearCosts: ->
            @setState {
                totalJita: 0
                totalJitaSplit: 0
            }
            
        render: ->
            tr { key: 'bomf-row' }, [
                th { key: 'bomf-ttl' }, "Total"
                th { key: 'bomf-spc', colSpan: 3}, ""
                th { key: 'bomf-ttl-jsl', style: { textAlign: 'right' } }, @state.totalJita.toLocaleString()
                th { key: 'bomf-ttl-jspl', style: { textAlign: 'right' } }, @state.totalJitaSplit.toLocaleString()
            ]
            
    BillOfMaterialsTitle = React.createClass
        getInitialState: ->
            {
                jitaSellPrice: NaN
            }
        
        getJitaPricing: ->
            jitaPriceURL = jsRoutes.controllers.MarketController.jitaPriceForItem(@props.itemID)
            $.ajax jitaPriceURL
            .done ((result) ->
                cost = result.sellPrice 
                this.setState { jitaSellPrice: cost }
                
            ).bind(this)
            .fail((jqXHR, textStatus, errorThrown) ->
                this.setState { jitaSellPrice: NaN }
            )
        
        efficiencyChanged: (event) ->
            @props.efficiencyChanged(event.target.value)
            
        render: ->
            displayName = @props.itemName + if @props.quantityProduced > 1 then " × " + @props.quantityProduced else ""
            displayJitaSell = if @state.jitaSellPrice == NaN
                "..."
            else
                (@state.jitaSellPrice * @props.quantityProduced).toLocaleString()
                    
            div { key: 'bomtitle', className: 'row' }, [
                div { key: 'tcol', className: 'col-md-4' }, [
                    h2 { key: 'th2' }, displayName
                ]
                div { key: 'pricecol', className: 'col-md-2' }, [
                    h2 { key: 'pch2'}, "Jita Sell " + displayJitaSell
                ]
                div { key: 'mecol', className: 'col-md-1 input-group input-group-sm'}, [
                    label { key: 'melbl', labelFor: 'bow-mein' }, "ME %"
                    input { 
                        id: 'bom-mein'
                        key: 'meinp'
                        type: 'text'
                        className: 'form-control'
                        placeholder: '00'
                        'aria-describedby': @props.key + 'lbl'
                        style: {
                            borderRadius: '4px'
                        }
                        maxLength: 2
                        onChange: @efficiencyChanged
                        value: @props.efficiency
                    }, null
                ]
            ]
            
        componentDidMount: ->
            @getJitaPricing()
            true
            
        componentWillReceiveProps: (nextProps) ->
            @setState @getInitialState()
            @getJitaPricing()
            true
            
    BlueprintDetails = React.createClass
        getInitialState: ->
            {
                billOfMaterials: []
                quantityProduced: 0
                materialEfficiency: 0
            }
            
        addCosts: (jitaCost, jitaSplitCost) ->
            @refs.bomFooter.addCosts(jitaCost, jitaSplitCost)
            
        loadBillOfMaterials: (itemID, itemName) ->
            bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct(itemID)
            $.ajax bomURL
            .done ((result) ->
                # Assuming any 200 is success
                @setState {
                    billOfMaterials: result.materials
                    quantityProduced: result.quantityProduced
                    itemName: itemName
                    itemID: itemID
                    totalJita: 0
                    totalJitaSplit: 0
                }
            ).bind(this)
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                alert "Error thrown: " + errorThrown
            )
            
        
        baseEfficiencyChanged: (newValue) ->
            # alert 'New Value: ' + newValue
            @setState { materialEfficiency: newValue }
        
        render: ->
            bomeFactory = React.createFactory BillOfMaterialsEntry
            bomfFactory = React.createFactory BillOfMaterialsFooter
            bomtFactory = React.createFactory BillOfMaterialsTitle
            
            if (@state.billOfMaterials.length > 0)
                ac = @addCosts
                i = 0
                bomMaterialEfficiency = @state.materialEfficiency
                div { key: 'bom' }, [
                    bomtFactory { 
                        key: 'ttl'
                        itemName: @state.itemName
                        itemID: @state.itemID 
                        quantityProduced: @state.quantityProduced
                        efficiencyChanged: @baseEfficiencyChanged
                    }, null
                    div { key: 'bomrow' + Math.random(), className: 'row' }, [
                        div { key: 'bomcol', className: 'col-md-10' }, [
                            table { key: 'bomtable', className: 'table' }, [
                                tr { key: 'bomheader' }, [
                                    th { key: 'q', style: { textAlign: 'center' } }, "Quantity"
                                    th { key: 'i' }, "Item"
                                    th { key: 'v', style: { textAlign: 'center' } }, "Volume"
                                    th { key: 'iid', style: { textAlign: 'center' } }, "Item ID"
                                    th { key: 'jsl', style: { textAlign: 'center' } }, "Jita Sell"
                                    th { key: 'jspl', style: { textAlign: 'center' } }, "Jita Split"
                                    th { key: 'spc' }, null
                                    th { key: 'me'}, "ME %"
                                ]
                                @state.billOfMaterials.map (row) ->
                                    row.addCosts = ac
                                    row.key = 'br-' + i
                                    row.ref = 'bome-' + i
                                    row.initialMaterialEfficiency = bomMaterialEfficiency
                                    row.quantityModifier = Math.pow(0.99, bomMaterialEfficiency)
                                    i += 1
                                    bomeFactory row, null
                                bomfFactory { key: @state.keyfix + '-bomf', ref: 'bomFooter'}, null
                            ]
                        ]
                    ]
                ]
            else
                div { key: 'bomrow' }, null
    BlueprintDetails