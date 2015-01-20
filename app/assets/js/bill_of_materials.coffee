define ['react'], (React) ->

    { div, h2, button, span, input } = React.DOM
    { table, tr, th, td, tbody } = React.DOM
    
    BillOfMaterialsEntry = React.createClass
        getInitialState: ->
            {
                jitaPrice: NaN
                jitaSplit: NaN
                subMaterials: []
                keyfix: Math.random()
                materialEfficiency: 0
            }

        getSubMaterialsClick: ->
            bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct @props.materialID
            $.ajax bomURL
            .done ((result) ->
                # Assuming any 200 is success
                # alert result
                @setState {
                    subMaterials: result
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
        
            if (@state.subMaterials.length > 0)
                subMaterials = @state.subMaterials.map (row) ->
                    row.addCosts = ac
                    row.isChild = true
                    row.quantityModifier = (q * me)
                    row.key = row.materialID
                    bomeFactory row, null
            else
                subMaterials = null
                
            displayableQuantity = @props.quantity
            if (@props.quantityModifier)
                displayableQuantity = Math.round(displayableQuantity * @props.quantityModifier)
                
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
                    displayableQuantity = Math.round(displayableQuantity * @props.quantityModifier)
                
                cost = result.sellPrice * displayableQuantity
                splitCost = split * displayableQuantity
                this.setState { jitaPrice: cost, jitaSplit: splitCost }
                ac(cost, splitCost)
                
            ).bind(this)
            .fail((jqXHR, textStatus, errorThrown) ->
                this.setState { jitaPrice: 'n/a', jitaSplit: 'n/a'}
            )
            
        componentDidMount: ->
            this.getJitaPricing()
            true
            
        componentWillReceiveProps: (nextProps) ->
            @setState {
                jitaPrice: NaN
                jitaSplit: NaN
                subMaterials: []
            }
            this.getJitaPricing()
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
        render: ->
            div { key: 'bomtitle', className: 'row' }, [
                div { key: 'tcol', className: 'col-md-4' }, [
                    h2 { key: 'th2' }, @props.itemName
                
                ]
            ]
            
    BlueprintDetails = React.createClass
        getInitialState: ->
            {
                billOfMaterials: []
            }
            
        addCosts: (jitaCost, jitaSplitCost) ->
            @refs.bomFooter.addCosts(jitaCost, jitaSplitCost)
            
        loadBillOfMaterials: (itemID, itemName) ->
            bomURL = jsRoutes.controllers.BlueprintController.materialsForProduct(itemID)
            $.ajax bomURL
            .done ((result) ->
                # Assuming any 200 is success
                @setState {
                    billOfMaterials: result
                    itemName: itemName
                    totalJita: 0
                    totalJitaSplit: 0
                }
            ).bind(this)
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                alert "Error thrown: " + errorThrown
            )
            
        render: ->
            bomeFactory = React.createFactory BillOfMaterialsEntry
            bomfFactory = React.createFactory BillOfMaterialsFooter
            bomtFactory = React.createFactory BillOfMaterialsTitle
            
            if (@state.billOfMaterials.length > 0)
                ac = @addCosts
                i = 0
                div { key: 'bom' }, [
                    bomtFactory { key: 'ttl', itemName: @state.itemName }, null
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