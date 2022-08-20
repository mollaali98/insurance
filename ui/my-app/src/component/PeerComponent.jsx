import React, {useState, useEffect} from 'react'
import PeerService from "../service/PeerService"
import AuthService from "../service/AuthService";

const PeerComponent = () => {

    const [peers, setPeers] = useState([])

    useEffect(() => {
        getPeers()
    }, [])

    const getPeers = () => {
        const user = AuthService.getCurrentUser()
        if (user.roles.includes("ROLE_INSURER")) {
            PeerService.getPeersInsurer().then((response) => {
                setPeers(response.data)
                console.log(response.data)
            })
        } else if (user.roles.includes("ROLE_INSUREE")) {
            PeerService.getPeersInsuree().then((response) => {
                setPeers(response.data)
                console.log(response.data)
            })
        }

    }

    return (
        <div className="container">

            <h1 className="text-center"> Peers List</h1>

            <table className="table table-striped">
                <thead>
                <tr>
                    <th> Peer Name</th>
                </tr>
                </thead>
                <tbody>
                {
                    peers.map(
                        peer =>
                            <tr key={peer}>
                                <td> {peer}</td>
                            </tr>
                    )
                }
                </tbody>
            </table>
        </div>
    )
}

export default PeerComponent