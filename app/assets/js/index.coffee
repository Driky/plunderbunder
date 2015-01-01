
asset_router = ar.controllers.Assets
        
{img, a, p} = React.DOM

LoginButton = React.createClass
    loggedIn: ->
        this.setState { logged_in: false }

    getInitialState: ->
        {
            logged_in: false
            character: null
            ajaxLoading: true
        }
        
    render: -> 
        eve_login_img_src = (asset_router.at "images/EVE_SSO_Login_Buttons_Large_White.png").url
        loginPath = kartelConfig.eve_login
        
        unless this.state.ajaxLoading
            unless this.state.logged_in
                a {"href": loginPath}, [
                    (img {"src": eve_login_img_src, "key":"img"}, null)
                ]
            else
                p {}, "Logged in as: " + this.state.character
        else
            p {}, "Loading..."
            
    componentDidMount: ->
        userApi = jsRoutes.controllers.Application.user()
        $.ajax userApi.url
        .done ((result) ->
            resJson = $.parseJSON(result)
            if this.isMounted()
                this.setState { logged_in: true, character: resJson.character + ' [' + resJson.token + ']',  ajaxLoading: false}
            ).bind this
        .fail ((jqXHR, textStatus, errorThrown) ->
            resultCode = jqXHR.status
            if this.isMounted()
                this.setState { logged_in: false, character: null, ajaxLoading: false }
        ).bind this
    
login = React.createElement LoginButton, null

rendered = React.render login, document.getElementById('content')


