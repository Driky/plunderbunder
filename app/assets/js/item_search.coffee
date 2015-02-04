define ['react'], (React) ->
    { div, input, span, button } = React.DOM
    
    ItemSearch = React.createClass
        getInitialState: ->
            {
            }

        handleClick: (event) ->
            itemSearch = this.refs.itemSearchInput.getDOMNode()
            itemName = $(itemSearch).val()
            itemID = @state.selectedItem
            this.props.onItemSelected { name: itemName, id: itemID }

        render: ->
            div { key: 'srch-row', className: 'row bottom7' }, [
                div { key: 'srch-col', className: 'col-md-5' }, [
                    div { key: 'in-grp', className: 'input-group' }, [
                        input {
                            key: 'srch-inp'
                            className: 'form-control itemInput'
                            ref: 'itemSearchInput'
                            type: 'text'
                            placeholder: 'What are you building?'
                        }, null
                        span { key: 'srch-btn-spn', className: 'input-group-btn' }, [
                            button {
                                key: 'srch-btn'
                                type: 'submit'
                                ref: 'itemSearchButton'
                                className: 'btn btn-primary'
                                onClick: @handleClick
                            }, "Show Details"
                        ]
                    ]
                ]
            ]
        componentDidMount: ->
            itemList = jsRoutes.controllers.Application.buildableInventoryItems().url

            engine = new Bloodhound {
                datumTokenizer: Bloodhound.tokenizers.obj.whitespace('name'),
                queryTokenizer: Bloodhound.tokenizers.whitespace,
                prefetch: itemList
            }
            engine.clearPrefetchCache()
            engine.initialize()

            itemSearchDom = this.refs.itemSearchInput.getDOMNode()
            searchButton = $(this.refs.itemSearchButton.getDOMNode())
            searchButton.prop('disabled', true)

            itemSearch = $(itemSearchDom)
            itemSearch.typeahead {
              minLength: 3
              highlight: true
            }, {
              name: 'all-items'
              source: engine.ttAdapter()
              displayKey: 'name'
            }

            component = this
            itemSearch.on "typeahead:selected typeahead:autocompleted", (e, datum) ->
                component.setState { selectedItem: datum.id }
                searchButton = $(component.refs.itemSearchButton.getDOMNode())
                searchButton.prop('disabled', false)

            # Reset the value in the event a user changes the text but it's not auto completed
            itemSearch.on "change", (e) ->
                searchButton = $(component.refs.itemSearchButton.getDOMNode())
                searchButton.prop('disabled', true)
                component.setState { selectedItem: null }
            true
            
    ItemSearch
