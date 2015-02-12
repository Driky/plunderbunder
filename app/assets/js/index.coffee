require.config {
    paths: {
        react: '../lib/react/react-with-addons'
    }
}

require [
    'react'
    'item_search'
    'login_button'
    'bill_of_materials'
    'edit_profile'
    'asset_list'
    ], (React, ItemSearch, LoginButton, BillOfMaterials, EditProfile, AssetList) -> 
    asset_router = ar.controllers.Assets

    {div, nav, a, ul, li} = React.DOM
    
    NO_MODE = 0
    BP_MODE = 1
    EP_MODE = 2
    AS_MODE = 3
    
    HeaderItem = React.createClass
        render: ->
            li { key: 'hdr-itm' }, [
                a { 
                    key: 'a'
                    href: '#'
                    role: 'button'
                    id: @props.id
                    onClick: @props.onClick
                }, @props.label
            ]
    MasterView = React.createClass
        getInitialState: ->
            {
                mode: 0
            }
    
        selectedItem: (item) ->
            this.refs.blueprintDetail.loadBillOfMaterials item.id, item.name
            
        setMode: (event) ->
            
            newMode = NO_MODE
            
            if (event.target.id == 'bpMode')
                newMode = BP_MODE
            else if (event.target.id == 'epMode')
                newMode = EP_MODE
            else if (event.target.id == 'asMode')
                newMode = AS_MODE
            
            @setState { mode: newMode }
            
        editProfile: ->
            @setMode { target: { id: 'epMode' } }
            
        contentForMode: ->
            isf = React.createFactory ItemSearch
            bdf = React.createFactory BillOfMaterials
            epf = React.createFactory EditProfile
            assets = React.createFactory AssetList
            
            if (@state.mode == BP_MODE)
                [
                    isf { key: 'app-isf', onItemSelected: this.selectedItem }, null

                    bdf { key: 'app-bdf', ref: 'blueprintDetail' }, null
                ]
            else if (@state.mode == EP_MODE)
                epf { key: 'epf', profile: @refs.loginbutton.state.profile }, null
            else if @state.mode == AS_MODE
                assets { key: 'assets' }, null
            else
                ""
            
        render: ->
            lb = React.createFactory LoginButton
            hi = React.createFactory HeaderItem
            
            bodyContent = @contentForMode()
            
            div { key: 'app' }, [
                nav { key: 'nav', className: 'navbar navbar-inverse navbar-fixed-top' }, [
                    div { key: 'navheader', className: 'navbar-header' }, [
                        a { 
                            key: 'brandname'
                            className: 'navbar-brand'
                            href: '#' 
                        }, "Plunderbunder"
                    ]
                    ul {
                        key: 'navleft'
                        className: 'nav navbar-nav'
                    }, [
                        hi {
                            key: 'bpomfg'
                            onClick: @setMode
                            id: 'bpMode'
                            label: "Blueprint Details"
                        }, null
                        hi {
                            key: 'assets'
                            onClick: @setMode
                            id: 'asMode'
                            label: "Assets"
                        }
                    ]
                    ul { key: 'navright', className: 'nav navbar-nav navbar-right' }, [
                        lb { 
                            key: 'loginbutton'
                            ref: 'loginbutton'
                            editProfile: @editProfile
                        }, null
                        li { 
                            key: 'loginpadding'
                            style: { paddingRight: '15px'}
                        }, ' '
                    ]
                ]
                div { key: 'appbody' }, bodyContent
            ]
            
    masterView = React.createElement MasterView, null
    rendered = React.render masterView, document.getElementById('content')
