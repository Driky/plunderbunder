define ['react'], (React) ->
    { div, h1, label, input, br, button } = React.DOM
    
    ReactCSSTransitionGroup = React.addons.CSSTransitionGroup
    
    AlertBox = React.createClass
        getInitialState: -> 
            {
                visible: false
            }
        render: ->
            if (@state.visible)
                @setTimer()
                children = div { 
                        key: 'ad'
                        className: 'alert alert-success positivemessage'
                    }, @state.message
            else
                children = div { key: 'da' }, null
                
            React.createElement ReactCSSTransitionGroup, {transitionName: 'fade'}, children
                
        componentDidMount: ->
            @setTimer()
          
        setTimer: ->
            # clear any existing timer
            @_timer = if @_timer != null then clearTimeout(@_timer) else null

            # hide after `delay` milliseconds
            @_timer = setTimeout( (() ->
                @setState({visible: false})
                @_timer = null
            ).bind(this), 1500)
                    
    EditProfile = React.createClass
        getInitialState: ->
            {
                profile: {}
            }
        
        reloadViaAjax: (userID) ->
            userProfileURL = jsRoutes.controllers.User.userProfile()
            userProfileURL.ajax()
            .done ((result) ->
                if this.isMounted()
                    @setState { profile: result,  ajaxLoading: false}
                ).bind this
            .fail ((jqXHR, textStatus, errorThrown) ->
                resultCode = jqXHR.status
                if this.isMounted()
                    @setState { profile: null, ajaxLoading: false }
            ).bind this
            
        componentDidMount: ->
            @reloadViaAjax(@props.profile.id)
            
        componentWillReceiveProps: (nextProps) ->
            @reloadViaAjax(nextProps.profile.id)
        
        formChanged: (event) ->
            etv = event.target.value
            et = event.target
            
            # Update the state, otherwise the input
            # won't accept characters
            profile = @state.profile
            if et.id == 'emailAddress'
                profile.email = etv
            else if et.id == 'keyID'
                profile.apiKey = etv
            else if et.id == 'keyVcode'
                profile.apiVCode = etv
            @setState profile
            
            setTimeout((() ->
                if (etv == et.value)
                    controlName = ""
                    data = {}
                    if et.id == 'emailAddress'
                        controlName = 'Email address'
                        data.emailAddress = etv
                    else if et.id == 'keyID'
                        controlName = 'API Key'
                        data.apiKey = parseInt(etv)
                    else if et.id == 'keyVcode'
                        controlName = 'API vCode'
                        data.apiVCode = etv
                        
                    updateProfileURL = jsRoutes.controllers.User.updateUserProfile()
                    updateProfileURL.ajax({
                        data: JSON.stringify(data)
                        contentType: 'application/json'
                    })
                    .done ((result) ->
                        if this.isMounted()
                            @refs.inp_alert.setState { message: controlName + ' saved', visible: true }
                        ).bind this
                    .fail ((jqXHR, textStatus, errorThrown) ->
                        resultCode = jqXHR.status
                        message = 'Error: ' + resultCode
                        @refs.inp_alert.setState { message: message, visible: true }
                    ).bind this
            ).bind(this), 600)
            
        render: ->
            console.log('rendering')
            af = React.createFactory AlertBox
            
            div {
                key: 'prf'
            }, [
                div {
                    key: 'headrow'
                    className: 'row'
                }, [
                    div {
                        key: 'header'
                        className: 'col-md-12'
                    }, h1 { key: 'hh1' }, "User Information"
                ]
                div {
                    key: 'keyidrow'
                    className: 'row'
                }, [
                    div {
                        key: 'col'
                        className: 'col-md-6'
                    }, [
                        div { key: 'fg', className: 'form-group' }, [
                            label { 
                                key: 'keyidlbl'
                                labelFor: 'keyID' 
                            }, "Key ID:"
                            input { 
                                key: 'keyid'
                                id: 'keyID'
                                type: 'text'
                                placeholder: 'Key ID'
                                className: 'form-control'
                                style: { position: 'relative' }
                                onChange: @formChanged
                                value: @state.profile.apiKey
                            }, null
                            label { 
                                key: 'keyvrfylbl'
                                labelFor: 'keyVcode' 
                            }, "vCode:"
                            input {
                                key: 'keyvrfy'
                                id: 'keyVcode'
                                type: 'text'
                                placeholder: 'vCode'
                                className: 'form-control'
                                onChange: @formChanged
                                value: @state.profile.apiVCode
                            }, null
                            label { 
                                key: 'lbl'
                                labelFor: 'emailAddress'
                            }, "Email Address:"
                            input {
                                key: 'inp'
                                title: 'Enter your email address for updates to the site.'
                                id: 'emailAddress'
                                type: 'text'
                                className: 'form-control'
                                placeholder: 'user@srs.bizn.as'
                                onChange: @formChanged
                                value: @state.profile.email
                            }, null
                            
                        ]
                    ]
                ]
                af { key: 'inp-alert', ref: 'inp_alert' }, null
            ]
            
    EditProfile